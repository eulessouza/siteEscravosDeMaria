import React from 'react'
import Header from '../components/Header'
import posts from '../data/posts'
import PostCard from '../components/PostCard'

export default function Home() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-4xl mx-auto p-6">
        <h2 className="text-3xl font-semibold text-gray-800 mb-6">Postagens recentes</h2>
        <div className="grid gap-4">
          {posts.map((p) => (
            <PostCard key={p.id} post={p} />
          ))}
        </div>
      </main>
    </div>
  )
}
