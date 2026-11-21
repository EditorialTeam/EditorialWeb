/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        editorial: {
          50: '#f8f8f7',
          100: '#efefe9',
          200: '#dddcd3',
          300: '#c5c3b5',
          400: '#aaa694',
          500: '#95907c',
          600: '#807a67',
          700: '#676253',
          800: '#545045',
          900: '#46433b',
          950: '#26241f',
        },
      },
      fontFamily: {
        serif: ['Georgia', 'Cambria', '"Times New Roman"', 'Times', 'serif'],
        sans: ['Inter', 'system-ui', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
