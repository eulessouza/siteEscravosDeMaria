import React, { useEffect, useState } from 'react'

export default function Popover({ anchorRef, open, onClose, children, offset = { x: 0, y: 8 }, innerRef }) {
  const [style, setStyle] = useState({})

  useEffect(() => {
    if (!open) return setStyle({ display: 'none' })
    const anchor = anchorRef?.current
    if (!anchor) return setStyle({ display: 'block', position: 'fixed', top: '20%', left: '50%', transform: 'translateX(-50%)' })

    const rect = anchor.getBoundingClientRect()
    const top = rect.bottom + offset.y + window.scrollY
    const left = rect.left + offset.x + window.scrollX
    setStyle({ position: 'absolute', top: `${top}px`, left: `${left}px`, zIndex: 60 })

    const handleScroll = () => {
      const r = anchor.getBoundingClientRect()
      setStyle({ position: 'absolute', top: `${r.bottom + offset.y + window.scrollY}px`, left: `${r.left + offset.x + window.scrollX}px`, zIndex: 60 })
    }

    window.addEventListener('scroll', handleScroll)
    window.addEventListener('resize', handleScroll)
    return () => {
      window.removeEventListener('scroll', handleScroll)
      window.removeEventListener('resize', handleScroll)
    }
  }, [anchorRef, open, offset.x, offset.y])

  // close when clicking outside anchor and popover
  useEffect(() => {
    if (!open) return
    const onDoc = (e) => {
      const target = e.target
      const anchorEl = anchorRef?.current
      const popEl = innerRef?.current
      const clickedAnchor = anchorEl && anchorEl.contains(target)
      const clickedPopover = popEl && popEl.contains(target)
      if (!clickedAnchor && !clickedPopover) onClose?.()
    }
    const onKey = (e) => {
      if (e.key === 'Escape') onClose?.()
    }
    document.addEventListener('click', onDoc)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('click', onDoc)
      document.removeEventListener('keydown', onKey)
    }
  }, [open, anchorRef, innerRef, onClose])

  if (!open) return null

  return (
    <div style={style} className="min-w-[200px] max-w-sm" ref={innerRef}>
      <div className="bg-white dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded shadow-lg p-3">
        <div className="relative">
          <button onClick={onClose} className="absolute right-0 top-0 text-sm text-gray-500">✕</button>
          {children}
        </div>
      </div>
    </div>
  )
}
