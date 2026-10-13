from blog.models import Post
from rest_framework import serializers

class PostSerializer(serializers.ModelSerializer):
    author_name = serializers.ReadOnlyField(source='author.username')

    class Meta:
        model = Post
        fields = ('author', 'author_name', 'title', 'text', 'created_date', 'published_date', 'image')