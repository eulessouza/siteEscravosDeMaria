import React from 'react'
import Button from './Button'
import { useAuth } from '../context/AuthContext'

function AuthButton() {
  const { user, loginWithDiscord, logout, isAuthenticated } = useAuth()

  if (isAuthenticated) {
    return (
      <Button onClick={logout} className="bg-red-500 hover:bg-red-600">
        Sair
      </Button>
    )
  }

  return (
    <Button onClick={loginWithDiscord} className="bg-green-500 hover:bg-green-600">
      Entrar com Discord
    </Button>
  )
}

export default AuthButton