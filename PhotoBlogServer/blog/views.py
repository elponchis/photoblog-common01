from django.contrib.auth.models import User
from django.db.models import Count, Q
from django.shortcuts import render, get_object_or_404, redirect
from django.utils import timezone
from .models import Post
from .forms import PostForm
from rest_framework import viewsets
from .serializers import PostSerializer

def sidebar_context():
    now = timezone.now()
    authors = (User.objects
               .annotate(post_count=Count('post', filter=Q(post__published_date__lte=now)))
               .filter(post_count__gt=0)
               .order_by('username'))
    recent_posts = Post.objects.filter(published_date__lte=now).order_by('-published_date')[:5]
    return {'authors': authors, 'recent_posts': recent_posts}

def post_list(request):
    query = request.GET.get('q', '').strip()
    posts = Post.objects.filter(
        published_date__lte=timezone.now()
    ).order_by('-published_date')
    if query:
        posts = posts.filter(title__icontains=query)
    return render(request, 'blog/post_list.html',
                  {'posts': posts, 'query': query, **sidebar_context()})

def author_posts(request, username):
    author = get_object_or_404(User, username=username)
    posts = Post.objects.filter(
        author=author, published_date__lte=timezone.now()
    ).order_by('-published_date')
    return render(request, 'blog/post_list.html', {
        'posts': posts,
        'current_author': author,
        **sidebar_context(),
    })

def post_detail(request, pk):
    post = get_object_or_404(Post, pk=pk)
    return render(request, 'blog/post_detail.html', {
        'post': post,
        'current_author': post.author,
        **sidebar_context(),
    })

def post_new(request):
    if request.method == "POST":
        form = PostForm(request.POST)
        if form.is_valid():
            post = form.save(commit=False)
            post.author = request.user
            post.published_date = timezone.now()
            post.save()
            return redirect('post_detail', pk=post.pk)
    else:
        form = PostForm()
    return render(request, 'blog/post_edit.html', {'form': form})

def post_edit(request, pk):
    post = get_object_or_404(Post, pk=pk)
    if request.method == "POST":
        form = PostForm(request.POST, instance=post)
        if form.is_valid():
            post = form.save(commit=False)
            post.author = request.user
            post.published_date = timezone.now()
            post.save()
            return redirect('post_detail', pk=post.pk)
    else:
        form = PostForm(instance=post)
    return render(request, 'blog/post_edit.html', {'form': form})

def js_test(request):
    return render(request, 'blog/js_test.html', {})

class IntruderImage(viewsets.ModelViewSet):
    queryset = Post.objects.all()
    serializer_class = PostSerializer