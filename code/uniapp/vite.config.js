import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import { readFileSync } from 'fs'
import { resolve } from 'path'

const pkg = JSON.parse(
  readFileSync(resolve(__dirname, 'package.json'), 'utf-8')
)

export default defineConfig({
  plugins: [uni()],
  define: {
    __APP_VERSION__: JSON.stringify(pkg.version)
  }
})
