import { useCallback, useRef, useState } from 'react'

/**
 * Thin wrapper around the Web Serial API (Chrome/Edge only) for talking to a
 * USB/serial thermal receipt printer directly from the browser - no local
 * print-bridge service needed. `send(bytes)` opens the port on first use
 * (prompting the user to pick the device) and reuses it afterwards.
 */
export function usePrinter() {
  const portRef = useRef(null)
  const [busy, setBusy] = useState(false)
  const supported = typeof navigator !== 'undefined' && 'serial' in navigator

  const send = useCallback(
    async (bytes) => {
      if (!supported) {
        throw new Error('Browser ini tidak mendukung Web Serial API (gunakan Chrome/Edge terbaru)')
      }
      setBusy(true)
      try {
        if (!portRef.current) {
          portRef.current = await navigator.serial.requestPort()
          await portRef.current.open({ baudRate: 9600 })
        }
        const writer = portRef.current.writable.getWriter()
        await writer.write(bytes)
        writer.releaseLock()
      } finally {
        setBusy(false)
      }
    },
    [supported],
  )

  return { send, supported, busy }
}
