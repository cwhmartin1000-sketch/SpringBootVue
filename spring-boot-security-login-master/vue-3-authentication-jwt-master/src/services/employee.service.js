import axios from 'axios';
import authHeader from './auth-header';

const API_URL = 'http://10.13.8.76:8080/api/emp';

class EmployeeService {
  getAll() {
    return axios.get(API_URL, { headers: authHeader() });
  }

  create(employee) {
    return axios.post(API_URL, employee, { headers: authHeader() });
  }

  update(id, employee) {
    return axios.put(`${API_URL}/${id}`, employee, { headers: authHeader() });
  }

  remove(id) {
    return axios.delete(`${API_URL}/${id}`, { headers: authHeader() });
  }
}

export default new EmployeeService();
