import React from 'react'
import './index.css'
import Home from './pages/Home'
import { ThemeProvider } from './context/ThemeContext'
import { AuthProvider } from './context/AuthContext'

export default function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <div className="App">
          <Home />
        </div>
      </AuthProvider>
    </ThemeProvider>
  )
}