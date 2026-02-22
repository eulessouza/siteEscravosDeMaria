/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{js,jsx}",
  ],
  theme: {
    extend: {
      colors: {
        'brand-light': '#4171AA',
        'brand-dark': '#1B2438',
      },
    },
  },
  plugins: [],
}
