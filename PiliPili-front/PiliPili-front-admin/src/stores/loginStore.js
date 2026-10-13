import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

const useLoginStore = defineStore('loginState', {
  state: () => {
    return {
      showLogin: false,
      userInfo: {},
      deviceId: null
    }

  },
  actions: {
    setLogin(show) {
      this.showLogin = show
    },
    saveUserInfo(info) {
      this.userInfo = info
    },
    saveDeviceId(deviceId) {
      this.deviceId = deviceId;
    }
  }
})

export {
  useLoginStore
}
