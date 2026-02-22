import React from 'react'

export default function Button({ href = '#', children, variant = 'primary', className = '', ...props }) {
  const base = 'inline-block px-3 py-1.5 rounded-md text-sm font-medium focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors'
  const variants = {
    primary: 'bg-white text-brand-light hover:bg-gray-100 focus:ring-white dark:bg-gray-700 dark:text-brand-light dark:hover:bg-gray-600 dark:focus:ring-brand-light',
    ghost: 'bg-transparent text-white hover:text-gray-100 focus:ring-white dark:text-white dark:hover:text-gray-300 dark:focus:ring-brand-light',
  }
  const classes = `${base} ${variants[variant] || variants.primary} ${className}`.trim()

  return (
    <a href={href} className={classes} {...props}>
      {children}
    </a>
  )
}
