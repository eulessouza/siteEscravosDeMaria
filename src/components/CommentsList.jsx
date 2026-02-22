import React from 'react'

export default function CommentsList({ items = [] }) {
  if (!Array.isArray(items) || items.length === 0) {
    return <div className="text-sm text-gray-600 dark:text-gray-300">Nenhum comentário ainda.</div>
  }

  return (
    <ul className="space-y-3">
      {items.map((c, i) => (
        <li key={i} className="p-2 bg-gray-50 dark:bg-gray-800 rounded border border-gray-100 dark:border-gray-600">
          <div className="text-sm font-medium text-brand-dark dark:text-gray-200">{c.user?.username || c.user || 'Anônimo'}</div>
          <div className="text-sm text-gray-700 dark:text-gray-300 mt-1">{c.text}</div>
        </li>
      ))}
    </ul>
  )
}
