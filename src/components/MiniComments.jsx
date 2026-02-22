import React, { useEffect, useState } from 'react'
import CommentsList from './CommentsList'
import { API_ENDPOINTS } from '../config/api'

export default function MiniComments({ postId, max = 2, onExpand }) {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(false)

  const base = API_ENDPOINTS.auth.me.replace('/auth/me', '')

  useEffect(() => {
    let mounted = true
    const load = async () => {
      try {
        setLoading(true)
        const res = await fetch(`${base}/posts/${postId}/comments?limit=${max}`)
        if (res.ok) {
          const data = await res.json()
          if (mounted) setItems(Array.isArray(data) ? data.slice(0, max) : [])
        }
      } catch (err) {
        console.warn('Erro ao buscar mini comentários:', err)
      } finally {
        if (mounted) setLoading(false)
      }
    }
    load()
    return () => { mounted = false }
  }, [postId, max, base])

  return (
    <div>
      {loading ? (
        <div className="text-sm text-gray-500">Carregando comentários...</div>
      ) : items.length === 0 ? (
        <div className="text-sm text-gray-600 dark:text-gray-300">Nenhum comentário ainda.</div>
      ) : (
        <div>
          <CommentsList items={items} />
          {items.length >= max && (
            <div className="mt-2 text-sm">
              <button onClick={onExpand} className="text-brand-light hover:underline">Ver mais comentários</button>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
