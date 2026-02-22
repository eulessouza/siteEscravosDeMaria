import React, { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { API_ENDPOINTS } from '../config/api'
import AlertBox from './AlertBox'
import Popover from './Popover'

export default function ReactionButton({ postId, initialLiked = false, initialCount = 0, initialUsers = [] }) {
  const { isAuthenticated, user, loginWithDiscord } = useAuth()
  const [liked, setLiked] = useState(initialLiked)
  const [count, setCount] = useState(initialCount)
  const [users, setUsers] = useState(initialUsers)
  const [showUsers, setShowUsers] = useState(false)
  const usersRef = React.useRef(null)
  const [showAlert, setShowAlert] = useState(false)
  const [loading, setLoading] = useState(false)

  const base = API_ENDPOINTS.auth.me.replace('/auth/me', '')

  const handleReaction = async () => {
    if (!isAuthenticated) {
      setShowAlert(true)
      return
    }

    try {
      setLoading(true)
      const token = localStorage.getItem('discord_token')
      const res = await fetch(`${base}/posts/${postId}/reactions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ action: liked ? 'remove' : 'add' }),
      })

      if (!res.ok) {
        // backend may be unavailable in dev; still update UI optimistically
        console.warn('reaction request failed', res.status)
      }

      // optimistic UI update
      if (liked) {
        setLiked(false)
        setCount((c) => Math.max(0, c - 1))
        setUsers((u) => u.filter((x) => x !== user?.username))
      } else {
        setLiked(true)
        setCount((c) => c + 1)
        setUsers((u) => [user?.username || 'Você', ...u])
      }
    } catch (err) {
      console.error('Erro ao reagir:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleShowUsers = () => {
    if (!showUsers && users.length === 0) {
      // load likers from API
      ;(async () => {
        try {
          const res = await fetch(`${base}/posts/${postId}/reactions`)
          if (res.ok) {
            const data = await res.json()
            const list = Array.isArray(data) ? data.map((u) => (u.user?.username || u.user || u)) : []
            setUsers(list)
            setCount(list.length)
          }
        } catch (err) {
          console.warn('Erro ao buscar quem curtiu:', err)
        } finally {
          setShowUsers(true)
        }
      })()
      return
    }

    setShowUsers((s) => !s)
  }

  return (
    <div>
      {showAlert && (
        <AlertBox title="Você não está logado" message="É necessário entrar com Discord para interagir." onClose={() => setShowAlert(false)} />
      )}

      <div className="flex items-center gap-3">
        <button
          onClick={handleReaction}
          disabled={loading}
          className={`flex items-center gap-2 ${liked ? 'text-red-500 scale-110' : 'text-brand-dark dark:text-gray-400 hover:text-red-500'}`}
          title={isAuthenticated ? 'Reagir' : 'Entre com Discord para reagir'}
        >
          <span className="text-xl">{liked ? '❤️' : '🤍'}</span>
        </button>

        <button ref={usersRef} onClick={handleShowUsers} className="text-sm font-medium text-brand-dark dark:text-gray-200 hover:underline" title="Ver quem curtiu">
          {count}
        </button>
      </div>
      <Popover anchorRef={usersRef} open={showUsers} onClose={() => setShowUsers(false)}>
        {users.length === 0 ? (
          <div className="text-sm text-gray-600 dark:text-gray-300">Nenhuma curtida.</div>
        ) : (
          <ul className="text-sm text-brand-dark dark:text-gray-200">
            {users.map((u, i) => (
              <li key={i} className="py-1">{u}</li>
            ))}
          </ul>
        )}
      </Popover>
    </div>
  )
}
