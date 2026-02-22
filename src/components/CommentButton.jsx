import React, { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { API_ENDPOINTS } from '../config/api'
import Popover from './Popover'

export default function CommentButton({ postId, initialCount = 0, initialUsers = [], onFocusComposer }) {
  const { isAuthenticated } = useAuth()
  const [count, setCount] = useState(initialCount)
  const [users, setUsers] = useState(initialUsers)
  const [showComments, setShowComments] = useState(false)
  const [loading, setLoading] = useState(false)

  const base = API_ENDPOINTS.auth.me.replace('/auth/me', '')

  const buttonRef = React.useRef(null)

  const toggleComments = async () => {
    if (!showComments) {
      try {
        setLoading(true)
        const res = await fetch(`${base}/posts/${postId}/comments`)
        if (res.ok) {
          const data = await res.json()
          const commenters = Array.isArray(data) ? data.map((c) => c.user?.username || c.user || 'Anônimo') : []
          setUsers(commenters)
          setCount(commenters.length)
        } else {
          console.warn('Falha ao buscar comentários', res.status)
        }
      } catch (err) {
        console.warn('Erro ao buscar comentários:', err)
      } finally {
        setLoading(false)
        setShowComments(true)
      }
    } else {
      setShowComments(false)
    }
  }

  return (
    <div>
      <div className="flex items-center gap-3">
        <button onClick={() => { if (onFocusComposer) onFocusComposer(); else toggleComments(); }} className="text-xl" title="Abrir comentários">💬</button>
        <button ref={buttonRef} onClick={toggleComments} className="text-sm font-medium text-brand-dark dark:text-gray-200 hover:underline" title="Ver comentários">{count}</button>
      </div>

      <Popover anchorRef={buttonRef} open={showComments} onClose={() => setShowComments(false)}>
        {loading ? (
          <div className="text-sm text-gray-500">Carregando...</div>
        ) : users.length === 0 ? (
          <div className="text-sm text-gray-600 dark:text-gray-300">Nenhum comentário.</div>
        ) : (
          <ul className="text-sm text-brand-dark dark:text-gray-200">
            {users.map((u, i) => (
              <li key={i} className="py-1 border-b border-gray-100 dark:border-gray-600">{u}</li>
            ))}
          </ul>
        )}
      </Popover>
    </div>
  )
}
