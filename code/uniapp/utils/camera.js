export function takePhoto() {
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count: 1,
      sourceType: ['camera'],
      sizeType: ['compressed'],
      success: (res) => {
        resolve(res.tempFilePaths[0])
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

export function chooseImage(count = 9) {
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count,
      sourceType: ['album'],
      sizeType: ['compressed'],
      success: (res) => {
        resolve(res.tempFilePaths)
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

export function compressImage(path, quality = 80) {
  return new Promise((resolve, reject) => {
    uni.compressImage({
      src: path,
      quality,
      success: (res) => {
        resolve(res.tempFilePath)
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

export function getBase64(path) {
  return new Promise((resolve, reject) => {
    uni.getFileSystemManager().readFile({
      filePath: path,
      encoding: 'base64',
      success: (res) => {
        resolve(res.data)
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

export function uploadImage(path, url) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || ''
    uni.uploadFile({
      url,
      filePath: path,
      name: 'file',
      header: {
        'Authorization': token ? 'Bearer ' + token : ''
      },
      success: (res) => {
        try {
          const data = JSON.parse(res.data)
          if (data.code === 0) {
            resolve(data.data)
          } else {
            reject(data)
          }
        } catch (e) {
          reject(e)
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

export default {
  takePhoto,
  chooseImage,
  compressImage,
  getBase64,
  uploadImage
}
