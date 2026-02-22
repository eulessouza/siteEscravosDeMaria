import React, { useEffect, useState } from 'react'
import CommentsList from './CommentsList'
import CommentComposer from './CommentComposer'
import { API_ENDPOINTS } from '../config/api'

export default function FullCommentsModal({ postId, open, onClose }) {
  const [comments, setComments] = useState([])
  const [loading, setLoading] = useState(false)

  const base = API_ENDPOINTS.auth.me.replace('/auth/me', '')

  useEffect(() => {
    if (!open) return
    let mounted = true
    const load = async () => {
      try {
        setLoading(true)
        const res = await fetch(`${base}/posts/${postId}/comments`)
        if (res.ok) {
          const data = await res.json()
          if (mounted) setComments(Array.isArray(data) ? data : [])
        }
      } catch (err) {
        console.warn('Erro ao carregar comentários:', err)
      } finally {
        if (mounted) setLoading(false)
      }
    }
    load()
    return () => { mounted = false }
  }, [open, postId, base])

  const handlePosted = (c) => {
    setComments((s) => [c, ...s])
  }

  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 backdrop-blur-sm bg-black/30" onClick={onClose} />

      <div className="relative w-full max-w-3xl mx-4 max-h-[90vh] overflow-auto p-4">
        <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-600 rounded-lg p-4">
          <div className="flex justify-between items-center mb-3">
            <h3 className="text-lg font-semibold text-brand-dark dark:text-gray-100">Comentários</h3>
            <button onClick={onClose} className="text-sm text-gray-500">Fechar</button>
          </div>

          {loading ? (
            <div className="text-sm text-gray-500">Carregando...</div>
          ) : (
            <CommentsList items={comments} />
          )}

          <div className="mt-4">
            <CommentComposer postId={postId} onPosted={handlePosted} />
          </div>
        </div>
      </div>
    </div>
  )
}
