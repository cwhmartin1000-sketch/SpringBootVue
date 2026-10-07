<template>
  <section class="lounge-page">
    <header class="lounge-heading">
      <div>
        <span class="eyebrow">多人即時聊天室</span>
        <h1>午休小屋</h1>
        <p>用方向鍵走走，和在線同事打聲招呼。</p>
      </div>
      <router-link class="back-link" to="/user">回留言版</router-link>
    </header>

    <section v-if="!joined" class="join-panel">
      <h2>挑一個角色加入</h2>
      <p>每次進入聊天室都可以重新選擇。</p>
      <div class="avatar-picker" role="group" aria-label="選擇角色">
        <button
          v-for="avatar in avatars"
          :key="avatar"
          type="button"
          class="avatar-choice"
          :class="{ selected: selectedAvatar === avatar }"
          :aria-pressed="selectedAvatar === avatar"
          @click="selectedAvatar = avatar"
        >{{ avatar }}</button>
      </div>
      <button class="primary-button" :disabled="connecting" @click="joinRoom">
        {{ connecting ? "正在連線…" : "進入房間" }}
      </button>
      <p v-if="errorMessage" class="error-message" role="alert">{{ errorMessage }}</p>
    </section>

    <section v-else class="room-panel">
      <div class="room-toolbar">
        <span class="connection-state" :class="{ connected: connected }">
          <i></i>{{ connectionLabel }}
        </span>
        <span class="online-count">目前 {{ participants.length }} 人在線</span>
      </div>

      <div class="room-layout">
        <div
          class="room-scene"
          role="application"
          aria-label="聊天室場景，使用方向鍵移動角色"
          tabindex="0"
        >
          <div class="scene-window"><span>☀️</span></div>
          <div class="scene-rug"></div>
          <div class="scene-plant plant-left">🪴</div>
          <div class="scene-plant plant-right">🌱</div>
          <div class="scene-sign">午休時間</div>

          <div
            v-for="participant in participants"
            :key="participant.id"
            class="room-player"
            :class="{ 'my-player': participant.id === selfId }"
            :style="{ left: participant.x + '%', top: participant.y + '%' }"
          >
            <div v-if="bubbles[participant.id]" class="speech-bubble">
              {{ bubbles[participant.id] }}
            </div>
            <span class="player-avatar">{{ participant.avatar }}</span>
            <span class="player-name">{{ participant.username }}</span>
          </div>
        </div>

        <aside class="online-panel">
          <h2>一起在線</h2>
          <div v-for="participant in participants" :key="participant.id" class="online-person">
            <span>{{ participant.avatar }}</span>
            <span>{{ participant.username }}</span>
            <b v-if="participant.id === selfId">你</b>
          </div>
        </aside>
      </div>

      <form class="message-composer" @submit.prevent="sendText">
        <input
          v-model="draft"
          type="text"
          maxlength="60"
          autocomplete="off"
          placeholder="說點什麼…按 Enter 發送，最多 60 字"
          :disabled="!connected"
        />
        <button type="submit" class="primary-button" :disabled="!connected || !draft.trim()">發送</button>
      </form>
      <div class="emoji-actions" aria-label="快速表情">
        <button v-for="emoji in quickEmojis" :key="emoji" type="button" :disabled="!connected" @click="sendEmoji(emoji)">
          {{ emoji }}
        </button>
      </div>
      <p v-if="errorMessage" class="room-error" role="status">{{ errorMessage }}</p>
    </section>
  </section>
</template>

<script>
const AVATARS = ["🐱", "🐻", "🐰", "🐼", "🐸", "🐥"];
const QUICK_EMOJIS = ["👋", "😊", "🎉", "❤️"];

export default {
  name: "LoungeRoom",
  data() {
    return {
      avatars: AVATARS,
      quickEmojis: QUICK_EMOJIS,
      selectedAvatar: AVATARS[0],
      participants: [],
      selfId: "",
      draft: "",
      bubbles: {},
      bubbleTimers: {},
      socket: null,
      joined: false,
      connecting: false,
      connected: false,
      shouldReconnect: false,
      retryCount: 0,
      reconnectTimer: null,
      heartbeatTimer: null,
      errorMessage: ""
    };
  },
  computed: {
    connectionLabel() {
      if (this.connected) return "已連線";
      if (this.connecting) return "連線中";
      return "重新連線中";
    }
  },
  mounted() {
    window.addEventListener("keydown", this.handleMovement);
  },
  beforeUnmount() {
    this.shouldReconnect = false;
    this.joined = false;
    this.clearConnectionTimers();
    Object.values(this.bubbleTimers).forEach(clearTimeout);
    window.removeEventListener("keydown", this.handleMovement);
    if (this.socket) this.socket.close(1000, "離開房間");
  },
  methods: {
    joinRoom() {
      const user = JSON.parse(localStorage.getItem("user") || "null");
      if (!user || !user.accessToken) {
        this.$router.push({ path: "/login", query: { redirect: "/lounge" } });
        return;
      }
      this.errorMessage = "";
      this.joined = true;
      this.shouldReconnect = true;
      this.retryCount = 0;
      this.connectSocket();
    },
    connectSocket() {
      if (!this.shouldReconnect) return;
      this.connecting = true;
      this.connected = false;

      const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
      const socketUrl = `${protocol}//${window.location.hostname}:8080/ws/lounge`;
      const socket = new WebSocket(socketUrl);
      this.socket = socket;

      socket.addEventListener("open", () => {
        const user = JSON.parse(localStorage.getItem("user") || "null");
        if (!user || !user.accessToken) {
          this.errorMessage = "登入狀態已失效，請重新登入。";
          this.shouldReconnect = false;
          socket.close(1000, "登入狀態失效");
          return;
        }
        socket.send(JSON.stringify({ type: "join", token: user.accessToken, avatar: this.selectedAvatar }));
        this.startHeartbeat();
      });

      socket.addEventListener("message", (event) => this.handleServerMessage(event));
      socket.addEventListener("close", (event) => {
        if (this.socket !== socket) return;
        this.connected = false;
        this.connecting = false;
        this.stopHeartbeat();
        this.participants = [];
        this.selfId = "";
        if (this.shouldReconnect && event.code !== 1008) this.scheduleReconnect();
      });
      socket.addEventListener("error", () => {
        this.errorMessage = "聊天室連線暫時中斷，正在嘗試重新連線。";
      });
    },
    handleServerMessage(event) {
      let message;
      try {
        message = JSON.parse(event.data);
      } catch (error) {
        return;
      }

      if (message.type === "snapshot") {
        this.selfId = message.selfId;
        this.participants = message.participants || [];
        this.connected = true;
        this.connecting = false;
        this.retryCount = 0;
        this.errorMessage = "";
      } else if (message.type === "participants") {
        this.participants = message.participants || [];
      } else if (message.type === "bubble") {
        this.showBubble(message.participantId, message.text, message.expiresInMs || 5000);
      } else if (message.type === "error") {
        this.errorMessage = message.message || "聊天室發生錯誤。";
        if (this.errorMessage.includes("登入已失效")) this.shouldReconnect = false;
      }
    },
    handleMovement(event) {
      if (!this.connected || !this.selfId) return;
      const activeTag = document.activeElement && document.activeElement.tagName;
      if (activeTag === "INPUT" || activeTag === "TEXTAREA") return;
      const direction = {
        ArrowUp: [0, -2.5],
        ArrowDown: [0, 2.5],
        ArrowLeft: [-2.5, 0],
        ArrowRight: [2.5, 0]
      }[event.key];
      if (!direction) return;

      event.preventDefault();
      const player = this.participants.find((item) => item.id === this.selfId);
      if (!player) return;
      player.x = Math.max(8, Math.min(92, player.x + direction[0]));
      player.y = Math.max(12, Math.min(88, player.y + direction[1]));
      this.send({ type: "move", x: player.x, y: player.y });
    },
    sendText() {
      const text = this.draft.trim();
      if (!text || text.length > 60) return;
      this.send({ type: "say", text });
      this.draft = "";
    },
    sendEmoji(emoji) {
      this.send({ type: "emoji", text: emoji });
    },
    send(payload) {
      if (this.socket && this.socket.readyState === WebSocket.OPEN) {
        this.socket.send(JSON.stringify(payload));
      }
    },
    showBubble(participantId, text, duration) {
      if (this.bubbleTimers[participantId]) clearTimeout(this.bubbleTimers[participantId]);
      this.bubbles[participantId] = text;
      this.bubbleTimers[participantId] = setTimeout(() => {
        delete this.bubbles[participantId];
        delete this.bubbleTimers[participantId];
      }, duration);
    },
    scheduleReconnect() {
      if (!this.shouldReconnect || this.reconnectTimer) return;
      this.retryCount += 1;
      const delay = Math.min(1000 * (2 ** Math.min(this.retryCount - 1, 3)), 8000);
      this.errorMessage = "連線中斷，正在重新加入房間…";
      this.reconnectTimer = setTimeout(() => {
        this.reconnectTimer = null;
        this.connectSocket();
      }, delay);
    },
    startHeartbeat() {
      this.stopHeartbeat();
      this.heartbeatTimer = setInterval(() => this.send({ type: "ping" }), 25000);
    },
    stopHeartbeat() {
      if (this.heartbeatTimer) clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    },
    clearConnectionTimers() {
      this.stopHeartbeat();
      if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }
};
</script>

<style scoped>
.lounge-page { max-width: 1120px; margin: 0 auto; color: #293246; }
.lounge-heading { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 1.2rem; }
.eyebrow { color: #7770c9; font-size: .75rem; font-weight: 800; letter-spacing: .12em; }
.lounge-heading h1 { margin: .25rem 0; font-size: clamp(2rem, 4vw, 3rem); font-weight: 800; }
.lounge-heading p, .join-panel p { color: #697386; margin: 0; }
.back-link { color: #5b61a8; font-weight: 700; }
.join-panel, .room-panel { padding: 1.25rem; border: 1px solid #e5e7f1; border-radius: 22px; background: #fff; box-shadow: 0 14px 35px rgba(37, 43, 74, .08); }
.join-panel { max-width: 570px; margin: 3rem auto; text-align: center; }
.join-panel h2 { font-size: 1.35rem; font-weight: 800; }
.avatar-picker { display: flex; justify-content: center; gap: .75rem; flex-wrap: wrap; margin: 1.4rem 0; }
.avatar-choice { width: 64px; height: 64px; border: 2px solid transparent; border-radius: 18px; background: #f4f3ff; font-size: 2rem; transition: transform .15s ease, border-color .15s ease; }
.avatar-choice:hover { transform: translateY(-3px); }
.avatar-choice.selected { border-color: #7770c9; background: #eeedff; }
.primary-button { border: 0; border-radius: 12px; padding: .75rem 1.1rem; background: #6658c8; color: #fff; font-weight: 800; }
.primary-button:disabled { opacity: .55; cursor: not-allowed; }
.error-message, .room-error { margin-top: .8rem !important; color: #b42318 !important; }
.room-toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: .8rem; }
.connection-state { color: #9a5b12; font-weight: 700; }
.connection-state i { display: inline-block; width: 9px; height: 9px; margin-right: .45rem; border-radius: 50%; background: #f0a13c; }
.connection-state.connected { color: #18845b; }
.connection-state.connected i { background: #20b97a; }
.online-count { color: #657086; font-size: .9rem; }
.room-layout { display: grid; grid-template-columns: minmax(0, 1fr) 200px; gap: .9rem; }
.room-scene { position: relative; min-height: 440px; overflow: hidden; border: 5px solid #a57a55; border-radius: 16px; outline: none; background-color: #f5dcae; background-image: linear-gradient(90deg, rgba(255,255,255,.15) 1px, transparent 1px), linear-gradient(rgba(255,255,255,.15) 1px, transparent 1px); background-size: 32px 32px; box-shadow: inset 0 0 0 5px #d4a875; }
.room-scene:focus-visible { box-shadow: inset 0 0 0 5px #d4a875, 0 0 0 3px #8b80e8; }
.scene-window { position: absolute; top: 7%; left: 50%; width: 100px; height: 60px; display: grid; place-items: center; transform: translateX(-50%); border: 6px solid #fff6dc; border-radius: 8px; background: #a7d9ee; font-size: 1.7rem; }
.scene-rug { position: absolute; left: 19%; right: 19%; top: 34%; bottom: 8%; border: 3px dashed rgba(159, 111, 67, .28); border-radius: 48%; background: rgba(229, 172, 112, .3); }
.scene-plant { position: absolute; z-index: 1; font-size: 2rem; }
.plant-left { left: 5%; bottom: 8%; }
.plant-right { right: 5%; top: 20%; }
.scene-sign { position: absolute; top: 9%; right: 7%; padding: .3rem .6rem; transform: rotate(4deg); border: 2px solid #cb9869; border-radius: 8px; background: #fff2ce; color: #805e3e; font-size: .8rem; font-weight: 800; }
.room-player { position: absolute; z-index: 2; display: flex; min-width: 54px; flex-direction: column; align-items: center; transform: translate(-50%, -50%); transition: left .12s linear, top .12s linear; }
.player-avatar { display: grid; width: 52px; height: 52px; place-items: center; border: 3px solid #fff; border-radius: 16px; background: #fff9e8; box-shadow: 0 4px 0 rgba(104, 73, 47, .2); font-size: 2rem; image-rendering: pixelated; }
.my-player .player-avatar { border-color: #7165d1; }
.player-name { max-width: 100px; overflow: hidden; margin-top: .2rem; padding: .12rem .42rem; border-radius: 999px; background: rgba(255,255,255,.92); color: #384052; font-size: .72rem; font-weight: 800; text-overflow: ellipsis; white-space: nowrap; }
.speech-bubble { position: absolute; bottom: calc(100% + 11px); left: 50%; max-width: 190px; padding: .48rem .7rem; transform: translateX(-50%); border: 2px solid #6658c8; border-radius: 14px; background: #fff; color: #30354b; font-size: .85rem; line-height: 1.35; overflow-wrap: anywhere; box-shadow: 0 4px 0 rgba(79, 67, 155, .14); white-space: nowrap; }
.speech-bubble::after { position: absolute; bottom: -8px; left: calc(50% - 6px); width: 12px; height: 12px; transform: rotate(45deg); border-right: 2px solid #6658c8; border-bottom: 2px solid #6658c8; background: #fff; content: ""; }
.online-panel { padding: .9rem; border-radius: 14px; background: #f7f6ff; }
.online-panel h2 { margin: 0 0 .75rem; font-size: 1rem; font-weight: 800; }
.online-person { display: flex; align-items: center; gap: .5rem; padding: .42rem 0; border-bottom: 1px solid #e9e7f6; font-size: .88rem; }
.online-person b { margin-left: auto; color: #7165d1; font-size: .72rem; }
.message-composer { display: flex; gap: .6rem; margin-top: .9rem; }
.message-composer input { min-width: 0; flex: 1; border: 1px solid #d9dbea; border-radius: 12px; padding: .7rem .85rem; }
.emoji-actions { display: flex; gap: .5rem; margin-top: .6rem; }
.emoji-actions button { width: 40px; height: 38px; border: 1px solid #e3e1f2; border-radius: 10px; background: #f7f6ff; font-size: 1.15rem; }
.emoji-actions button:disabled { opacity: .5; }
@media (max-width: 760px) { .room-layout { grid-template-columns: 1fr; } .room-scene { min-height: 370px; } .online-panel { display: flex; flex-wrap: wrap; gap: .25rem .8rem; } .online-panel h2 { flex-basis: 100%; } .lounge-heading { align-items: flex-start; } }
@media (max-width: 480px) { .room-panel, .join-panel { padding: .8rem; } .room-scene { min-height: 330px; } .room-toolbar { align-items: flex-start; gap: .4rem; flex-direction: column; } }
</style>
