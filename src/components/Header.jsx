import React from 'react'
import Button from './Button'
import { useTheme } from '../context/ThemeContext'
import { useAuth } from '../context/AuthContext'
import AuthButton from './AuthButton'

export default function Header() {
  const { isDark, toggleTheme } = useTheme()
  const { user, isAuthenticated, loginWithDiscord, logout } = useAuth()

  return (
    <header className="bg-brand-light dark:bg-brand-dark text-white sticky top-0 z-50 shadow-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-4">
            <img src="https://media.discordapp.net/attachments/1474804664330485927/1474880979398299809/VUYK0Iw.webp?ex=699b7573&is=699a23f3&hm=4bffee866d6937f28591f9881b13ce76c2378d35896780b9b0b61023a9da56f3&=&format=webp" 
              alt="Escravos de Maria Logo" className="w-14 h-14 rounded-lg"/>
            <div>
              <h1 className="text-3xl text-white font-bold">Escravos de Maria</h1>
              <p className="text-sm text-white">Um Blog feito por Católicos que amam a Igreja</p>
            </div>
          </div>

          <div className="flex items-center space-x-2">
            <nav className="hidden md:flex items-center space-x-1">
              <Button href="/" variant="ghost">Início</Button>
              <Button href="/rosario" variant="ghost">Rosário</Button>
              <Button href="/publicacoes" variant="ghost">Publicações</Button>
              <Button href="/forum" variant="ghost">Fórum</Button>
            </nav>

            <AuthButton />

            <button
              onClick={toggleTheme}
              className="p-2 rounded-lg bg-opacity-20 hover:bg-opacity-30 transition-all"
              title={isDark ? 'Modo claro' : 'Modo escuro'}
            >
              {isDark ? '☀️' : '🌙'}
            </button>

            <div className="md:hidden">
              <Button href="#" variant="ghost">☰</Button>
            </div>
          </div>
        </div>
      </div>
    </header>
  )
}
