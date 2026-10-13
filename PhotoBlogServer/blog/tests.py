import io
import json

from django.contrib.auth.models import User
from django.test import TestCase
from django.urls import reverse
from django.utils import timezone
from PIL import Image
from rest_framework.authtoken.models import Token
from rest_framework.test import APIClient

from .models import Post

API_URL = '/api_root/Post/'


def make_image_file(name='test.jpg', size=(100, 80), color=(200, 60, 60)):
    """테스트용 이미지 파일을 메모리에 생성한다."""
    buffer = io.BytesIO()
    Image.new('RGB', size, color).save(buffer, format='JPEG')
    buffer.seek(0)
    buffer.name = name
    return buffer


class PostApiTest(TestCase):

    def setUp(self):
        self.user = User.objects.create_user(
            username='tester', email='tester@example.com', password='test1234'
        )
        self.token = Token.objects.create(user=self.user)

        self.client = APIClient()
        self.auth_client = APIClient()
        self.auth_client.credentials(HTTP_AUTHORIZATION='Token ' + self.token.key)

    def create_post(self, title='테스트 게시물'):
        return Post.objects.create(
            author=self.user,
            title=title,
            text='본문',
            created_date=timezone.now(),
            published_date=timezone.now(),
        )

    def test_get_returns_200(self):
        response = self.auth_client.get(API_URL)
        self.assertEqual(response.status_code, 200)

    def test_get_returns_json_array(self):
        self.create_post()
        response = self.auth_client.get(API_URL)
        data = json.loads(response.content)
        self.assertIsInstance(data, list)
        self.assertEqual(len(data), 1)

    def test_get_item_has_required_fields(self):
        self.create_post()
        response = self.auth_client.get(API_URL)
        item = json.loads(response.content)[0]

        for field in ('author', 'title', 'text',
                      'created_date', 'published_date', 'image'):
            self.assertIn(field, item)

    def test_allowed_methods(self):
        response = self.auth_client.options(API_URL)
        allow = response.headers.get('Allow', '')
        for method in ('GET', 'POST', 'HEAD', 'OPTIONS'):
            self.assertIn(method, allow)

    def test_post_creates_item(self):
        now = timezone.now().isoformat()
        payload = {
            'author': self.user.id,
            'title': 'curl 테스트',
            'text': 'API 자동 테스트로 작성된 게시물',
            'created_date': now,
            'published_date': now,
            'image': make_image_file(),
        }

        response = self.auth_client.post(API_URL, payload, format='multipart')

        self.assertIn(response.status_code, (200, 201))
        self.assertEqual(Post.objects.count(), 1)
        self.assertEqual(Post.objects.first().title, 'curl 테스트')

    def test_post_saves_image(self):
        now = timezone.now().isoformat()
        payload = {
            'author': self.user.id,
            'title': '이미지 포함',
            'text': '이미지 업로드 확인',
            'created_date': now,
            'published_date': now,
            'image': make_image_file('photo.jpg'),
        }

        self.auth_client.post(API_URL, payload, format='multipart')
        post = Post.objects.first()

        self.assertTrue(post.image)
        self.assertIn('photo', post.image.name)

    def test_image_url_in_response(self):
        now = timezone.now().isoformat()
        self.auth_client.post(API_URL, {
            'author': self.user.id,
            'title': 'URL 확인',
            'text': '본문',
            'created_date': now,
            'published_date': now,
            'image': make_image_file(),
        }, format='multipart')

        response = self.auth_client.get(API_URL)
        item = json.loads(response.content)[0]

        self.assertTrue(item['image'].startswith('http'))

    def test_post_with_new_author_name_creates_user(self):
        now = timezone.now().isoformat()
        response = self.auth_client.post(API_URL, {
            'author_name': '새작성자',
            'title': '이름으로 게시',
            'text': '본문',
            'created_date': now,
            'published_date': now,
            'image': make_image_file(),
        }, format='multipart')

        self.assertEqual(response.status_code, 201)
        self.assertTrue(User.objects.filter(username='새작성자').exists())
        self.assertEqual(Post.objects.first().author.username, '새작성자')
        self.assertEqual(json.loads(response.content)['author_name'], '새작성자')

    def test_post_with_existing_author_name_reuses_user(self):
        now = timezone.now().isoformat()
        self.auth_client.post(API_URL, {
            'author_name': 'tester',
            'title': '기존 작성자',
            'text': '본문',
            'created_date': now,
            'published_date': now,
            'image': make_image_file(),
        }, format='multipart')

        self.assertEqual(User.objects.count(), 1)
        self.assertEqual(Post.objects.first().author, self.user)

    def test_post_without_title_fails(self):
        now = timezone.now().isoformat()
        response = self.auth_client.post(API_URL, {
            'author': self.user.id,
            'text': '제목 없음',
            'created_date': now,
            'published_date': now,
        }, format='multipart')

        self.assertEqual(response.status_code, 400)
        self.assertEqual(Post.objects.count(), 0)


class PostListViewTest(TestCase):
    """웹 페이지(브라우저 화면) 동작 확인"""

    def setUp(self):
        self.user = User.objects.create_user(username='viewer', password='test1234')

    def test_post_list_page_opens(self):
        response = self.client.get('/')
        self.assertEqual(response.status_code, 200)

    def test_published_post_shown(self):
        Post.objects.create(
            author=self.user,
            title='게시된 글',
            text='본문',
            created_date=timezone.now(),
            published_date=timezone.now(),
        )

        response = self.client.get('/')
        self.assertContains(response, '게시된 글')

    def test_unpublished_post_hidden(self):
        Post.objects.create(
            author=self.user,
            title='임시 저장 글',
            text='본문',
            created_date=timezone.now(),
        )

        response = self.client.get('/')
        self.assertNotContains(response, '임시 저장 글')
