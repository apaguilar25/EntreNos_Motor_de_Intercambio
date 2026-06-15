export class ClienteHttp {
  constructor(baseUrl) {
    this.baseUrl = baseUrl;
  }

  _getHeaders() {
    const headers = {
      'Content-Type': 'application/json',
    };
    const token = localStorage.getItem('entreNosToken');
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
  }

  async get(endpoint) {
    const response = await fetch(`${this.baseUrl}${endpoint}`, {
      headers: this._getHeaders(),
    });
    if (!response.ok) {
      throw new Error(`Error GET ${endpoint}: ${response.statusText}`);
    }
    if (response.status === 204) {
      return null;
    }
    return response.json();
  }

  async post(endpoint, data) {
    const response = await fetch(`${this.baseUrl}${endpoint}`, {
      method: 'POST',
      headers: this._getHeaders(),
      body: JSON.stringify(data),
    });
    if (!response.ok) {
      throw new Error(`Error POST ${endpoint}: ${response.statusText}`);
    }
    if (response.status === 204) {
      return null;
    }
    return response.json();
  }
}
