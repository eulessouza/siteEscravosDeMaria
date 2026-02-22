import React, { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import EmojiPickerButton from './EmojiPickerButton'
import { API_ENDPOINTS } from '../config/api'

export default function CommentComposer({ postId, onPosted, inputRef }) {
  const { isAuthenticated, user } = useAuth()
  const [text, setText] = useState('')
  const [loading, setLoading] = useState(false)

  const base = API_ENDPOINTS.auth.me.replace('/auth/me', '')

  const addEmoji = (emoji) => setText((t) => t + emoji)

  const submit = async () => {
    if (!isAuthenticated) {
      // let AuthButton/Alert handle login flow elsewhere; here just abort
      return
    }
    if (!text.trim()) return

    try {
      setLoading(true)
      const token = localStorage.getItem('discord_token')
      const res = await fetch(`${base}/posts/${postId}/comments`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ text: text.trim() }),
      })

      if (!res.ok) {
        console.warn('Falha ao enviar comentário', res.status)
      }

      const username = user?.username || 'Você'
      onPosted?.({ user: { username }, text: text.trim() })
      setText('')
    } catch (err) {
      console.error('Erro ao enviar comentário:', err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex items-start gap-3 w-full">
      <div className="flex-shrink-0">
        <EmojiPickerButton onSelect={addEmoji} />
      </div>

      <div className="flex-1">
        <div className="flex gap-2">
            <input
              ref={inputRef}
              value={text}
              onChange={(e) => setText(e.target.value)}
              placeholder="Escreva um comentário"
              className="flex-1 p-2 rounded bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-600 text-sm"
            />
          <button onClick={submit} disabled={loading || !text.trim()} className="px-3 py-1.5 bg-brand-light text-white rounded">Enviar</button>
        </div>
      </div>
    </div>
  )
}
