import React from 'react'

export default function Button({ href = '#', children, variant = 'primary', className = '', ...props }) {
  const base = 'inline-block px-3 py-1.5 rounded-md text-sm font-medium focus:outline-none focus:ring-2 focus:ring-offset-2'
  const variants = {
    primary: 'bg-white text-blue-700 hover:bg-gray-100 focus:ring-white',
    ghost: 'bg-transparent text-white hover:text-gray-200 focus:ring-white',
  }
  const classes = `${base} ${variants[variant] || variants.primary} ${className}`.trim()

  return (
    <a href={href} className={classes} {...props}>
      {children}
    </a>
  )
}
