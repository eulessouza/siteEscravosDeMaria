import React from 'react'
import Button from './Button'


export default function PostCard({ post }) {
  return (
    <article className="border border-gray-200 dark:border-gray-700 rounded-lg p-4 bg-gray-50 dark:bg-gray-800 shadow-sm dark:shadow-lg hover:shadow-md dark:hover:shadow-xl transition-shadow">
      <div className="flex flex-col sm:flex-row sm:items-start sm:space-x-6">
        {post.img && (
          <img src={post.img} alt={post.title} className="w-32 h-32 object-cover rounded-lg mb-2 sm:mb-0 flex-shrink-0 mx-auto sm:mx-0" />
        )}

        <div className="flex-1 min-w-0">
          <h3 className="text-2xl font-bold text-blue-700 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 mb-2">
            <a href={post.url} target="_blank" rel="noopener noreferrer" className="hover:underline">{post.title}</a>
          </h3>
          <p className="text-sm text-gray-500 dark:text-gray-400 mb-3">{post.date}</p>
          <p className="text-base text-gray-700 dark:text-gray-300 leading-relaxed">{post.excerpt}</p>
        </div>
      </div>
    </article>
  )
}
