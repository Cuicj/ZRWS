export function getCurrentLocation() {
  return new Promise((resolve, reject) => {
    uni.getLocation({
      type: 'gcj02',
      isHighAccuracy: true,
      highAccuracyExpireTime: 3000,
      success: (res) => {
        resolve({
          lat: res.latitude,
          lng: res.longitude,
          altitude: res.altitude || 0,
          accuracy: res.accuracy || 0,
          speed: res.speed || 0,
          timestamp: Date.now()
        })
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

export function calculateDistance(lat1, lng1, lat2, lng2) {
  const R = 6371000
  const toRad = (deg) => (deg * Math.PI) / 180
  const dLat = toRad(lat2 - lat1)
  const dLng = toRad(lng2 - lng1)
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
    Math.sin(dLng / 2) * Math.sin(dLng / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return Math.round(R * c)
}

export function formatCoordinate(lat, lng) {
  const format = (val, dir) => {
    const abs = Math.abs(val)
    const d = Math.floor(abs)
    const m = Math.floor((abs - d) * 60)
    const s = (((abs - d) * 60) - m) * 60
    return `${d}°${m}'${s.toFixed(2)}"${val >= 0 ? dir : dir === 'N' ? 'S' : 'W'}`
  }
  return `${format(lat, 'N')}, ${format(lng, 'E')}`
}

let watcher = null

export function watchPosition(callback) {
  if (watcher) {
    stopWatch()
  }
  watcher = uni.onLocationChange((res) => {
    callback({
      lat: res.latitude,
      lng: res.longitude,
      altitude: res.altitude || 0,
      accuracy: res.accuracy || 0,
      speed: res.speed || 0,
      timestamp: Date.now()
    })
  })
  return watcher
}

export function stopWatch() {
  if (watcher) {
    uni.offLocationChange(watcher)
    watcher = null
  }
}

export default {
  getCurrentLocation,
  calculateDistance,
  formatCoordinate,
  watchPosition,
  stopWatch
}
