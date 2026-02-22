import React, { useState, useRef, useEffect } from 'react'
import Picker from 'emoji-picker-react'
import Popover from './Popover'

export default function EmojiPickerButton({ onSelect }) {
  const [open, setOpen] = useState(false)
  const buttonRef = useRef(null)
  const popoverRef = useRef(null)

  useEffect(() => {
    const onDoc = (e) => {
      const target = e.target
      const insideButton = buttonRef.current && buttonRef.current.contains(target)
      const insidePopover = popoverRef.current && popoverRef.current.contains(target)
      if (!insideButton && !insidePopover) setOpen(false)
    }
    document.addEventListener('click', onDoc)
    return () => document.removeEventListener('click', onDoc)
  }, [])

  const handlePick = (emojiData, event) => {
    const emoji = emojiData?.emoji || ''
    onSelect?.(emoji)
    // keep picker open so user can pick multiple; do not auto-close
  }

  return (
    <div className="inline-block">
      <button ref={buttonRef} type="button" onClick={() => setOpen((s) => !s)} className="p-1 text-xl">
        😀
      </button>

      <Popover anchorRef={buttonRef} innerRef={popoverRef} open={open} onClose={() => setOpen(false)}>
        <div className="w-64">
          <Picker onEmojiClick={handlePick} />
        </div>
      </Popover>
    </div>
  )
}
