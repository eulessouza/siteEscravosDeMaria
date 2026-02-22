import React from 'react'
import Header from '../components/Header'
import posts from '../data/posts'

import PostCard from '../components/PostCard'

export default function Home() {
  return (
    <div className="min-h-screen bg-white dark:bg-gray-900">
      <Header />
      <main className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <h2 className="text-4xl font-bold text-gray-900 dark:text-white mb-8">Postagens recentes</h2>
        <div className="grid gap-6">
          {posts.map((p) => (
            <PostCard key={p.id} post={p} />
          ))}
        </div>
      </main>
    </div>
  )
}
