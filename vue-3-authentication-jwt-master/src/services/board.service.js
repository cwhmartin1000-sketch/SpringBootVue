import axios from 'axios';
import authHeader from './auth-header';

const API_URL = 'http://10.13.8.76:8080/api';

class BoardService {
    getPosts() {
        return axios.get(API_URL + '/posts', { headers: authHeader() });
    }

    getHotPosts() {
        return axios.get(API_URL + '/posts/hot', { headers: authHeader() });
    }

    createPost(payload) {
        return axios.post(API_URL + '/posts', payload, { headers: authHeader() });
    }

    deletePost(postId) {
        return axios.delete(API_URL + `/posts/${postId}`, { headers: authHeader() });
    }

    addReply(postId, payload) {
        return axios.post(API_URL + `/posts/${postId}/replies`, payload, { headers: authHeader() });
    }

    toggleReaction(postId, reactionKey) {
        return axios.put(API_URL + `/posts/${postId}/reaction`, { reactionKey }, { headers: authHeader() });
    }
}

export default new BoardService();
