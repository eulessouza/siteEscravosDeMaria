import React from 'react'
import Button from './Button'

export default function PostCard({ post }) {
  return (
    <article className="border border-gray-600 rounded-lg p-4 bg-gray-900 text-gray-100 shadow">
      <div className="flex flex-col sm:flex-row sm:items-start sm:space-x-4">
        <div className="flex-1 min-w-0">
          <h3 className="text-xl font-semibold text-blue-300 break-words">
            <a href={post.url} target="_blank" rel="noopener noreferrer" className="hover:underline">{post.title}</a>
          </h3>
          <p className="text-sm text-gray-400 mb-2">{post.date}</p>
          <div className="flex flex-row sm:flex-row sm:items-start sm:space-x-4">
            {post.img && (
          <div className="flex-shrink-0 mb-3 sm:mb-0">
            <img src={post.img} alt={post.title} className="w-24 h-24 object-cover rounded-md" />
          </div>
            )}
            <p className="text-gray-300">{post.excerpt}</p>
          </div>
          <div className="mt-4 flex space-x-2">
            <Button href="" variant="secondary">Deixe um comentário</Button>
          </div>
        </div>
      </div>
    </article>
  )
}
