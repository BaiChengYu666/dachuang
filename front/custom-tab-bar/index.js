Component({
  data: {
    selected: 0,
    list: [
      { pagePath: "/pages/index/index", text: "首页", icon: "/static/icon/home.png", activeIcon: "/static/icon/home-active.png" },
      { pagePath: "/pages/behavior/behavior", text: "行为", icon: "/static/icon/behavior.png", activeIcon: "/static/icon/behavior-active.png" },
      { pagePath: "/pages/ai/ai", text: "AI", icon: "/static/icon/ai.png", activeIcon: "/static/icon/ai-active.png" },
      { pagePath: "/pages/risk/risk", text: "风险", icon: "/static/icon/risk.png", activeIcon: "/static/icon/risk-active.png" },
      { pagePath: "/pages/settings/settings", text: "设置", icon: "/static/icon/we.png", activeIcon: "/static/icon/we-active.png" }
    ]
  },
  methods: {
    switchTab(e) {
      const index = e.currentTarget.dataset.index
      const item = this.data.list[index]
      wx.switchTab({ url: item.pagePath })
    },
    setSelected(index) {
      this.setData({ selected: index })
    }
  }
})
