import React, { useState } from 'react'
import Button from './Button'
import { useAuth } from '../context/AuthContext'
import ReactionButton from './ReactionButton'
import CommentButton from './CommentButton'
import CommentComposer from './CommentComposer'
import MiniComments from './MiniComments'
import FullCommentsModal from './FullCommentsModal'


export default function PostCard({ post }) {
  const { isAuthenticated } = useAuth()
  const [reactionCount, setReactionCount] = useState(post.reactions || 0)
  const [commentCount, setCommentCount] = useState(post.comments || 0)
  const [reactionUsers, setReactionUsers] = useState(post.reactionUsers || [])
  const [showAllComments, setShowAllComments] = useState(false)
  const openCommentsModal = () => setShowAllComments(true)
  const closeCommentsModal = () => setShowAllComments(false)
  const composerInputRef = React.useRef(null)

  const handleReaction = async () => {
    if (!isAuthenticated) {
      loginWithDiscord()
      return
    }

    try {
      setLoading(true)
      const token = localStorage.getItem('discord_token')
      
      const response = await fetch(`${API_ENDPOINTS.auth.me.replace('/auth/me', '')}/posts/${post.id}/reactions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          action: liked ? 'remove' : 'add',
        }),
      })

      if (response.ok) {
        setLiked(!liked)
        setReactionCount(liked ? reactionCount - 1 : reactionCount + 1)
      }
    } catch (err) {
      console.error('Erro ao reagir:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleCommentClick = () => {
    if (!isAuthenticated) {
      loginWithDiscord()
      return
    }
    // TODO: Abrir modal/section de comentários ou navegar para página de detalhes
    // TODO: GET /api/posts/{postId}/comments
    console.log('Abrir comentários para post:', post.id)
  }

  return (
    <article className="border border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden bg-gray-50 dark:bg-gray-800 shadow-sm dark:shadow-lg hover:shadow-md dark:hover:shadow-xl transition-shadow flex flex-col">
      {post.img && (
        <img src={post.img} alt={post.title} className="w-full h-64 object-cover" />
      )}

      <div className="flex-1 min-w-0 p-4 flex flex-col">
        <div>
          <h3 className="text-2xl font-bold text-brand-dark dark:text-blue-400 hover:opacity-80 dark:hover:text-blue-300 mb-2">
            <a href={post.url} target="_blank" rel="noopener noreferrer" className="hover:underline">{post.title}</a>
          </h3>
          <p className="text-sm text-brand-dark dark:text-gray-400 mb-3">{post.date}</p>
          <p className="text-base text-brand-dark dark:text-gray-300 leading-relaxed">{post.excerpt}</p>
        </div>

        {/* Interaction buttons - Instagram style */}
        <div className="flex items-center gap-6 mt-4 pt-4 border-t border-gray-200 dark:border-gray-700 w-full">
          <div className="flex items-center gap-3">
            <ReactionButton postId={post.id} initialLiked={false} initialCount={reactionCount} initialUsers={reactionUsers} />
          </div>

          <div className="flex items-center gap-3">
            <CommentButton
              postId={post.id}
              initialCount={commentCount}
              onFocusComposer={() => {
                if (composerInputRef.current) {
                  composerInputRef.current.scrollIntoView({ behavior: 'smooth', block: 'center' })
                  composerInputRef.current.focus()
                }
              }}
            />
          </div>
        </div>

        <div className="mt-3">
          <CommentComposer
            postId={post.id}
            inputRef={composerInputRef}
            onPosted={(newComment) => {
              setCommentCount((c) => c + 1)
            }}
          />

          <div className="mt-3">
            <MiniComments postId={post.id} max={2} onExpand={openCommentsModal} />
          </div>
        </div>

                
      </div>
    </article>
  )
}
