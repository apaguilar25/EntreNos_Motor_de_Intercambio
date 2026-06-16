export class ClienteHttp {
  constructor(baseUrl) {
    this.baseUrl = baseUrl;
  }

  _getHeaders() {
    const headers = {
      'Content-Type': 'application/json',
    };
    const token = sessionStorage.getItem('entreNosToken');
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
      const errorText = await response.text();
      throw new Error(`Error GET ${endpoint}: ${errorText || response.statusText}`);
    }
    if (response.status === 204) return null;
    const text = await response.text();
    try { return JSON.parse(text); } catch { return text; }
  }

  async post(endpoint, data) {
    const response = await fetch(`${this.baseUrl}${endpoint}`, {
      method: 'POST',
      headers: this._getHeaders(),
      body: JSON.stringify(data),
    });
    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Error POST ${endpoint}: ${errorText || response.statusText}`);
    }
    if (response.status === 204) return null;
    const text = await response.text();
    try { return JSON.parse(text); } catch { return text; }
  }

  async put(endpoint, data) {
    const response = await fetch(`${this.baseUrl}${endpoint}`, {
      method: 'PUT',
      headers: this._getHeaders(),
      body: JSON.stringify(data),
    });
    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Error PUT ${endpoint}: ${errorText || response.statusText}`);
    }
    if (response.status === 204) return null;
    const text = await response.text();
    try { return JSON.parse(text); } catch { return text; }
  }

  async delete(endpoint) {
    const response = await fetch(`${this.baseUrl}${endpoint}`, {
      method: 'DELETE',
      headers: this._getHeaders(),
    });
    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Error DELETE ${endpoint}: ${errorText || response.statusText}`);
    }
    if (response.status === 204) return null;
    const text = await response.text();
    try { return JSON.parse(text); } catch { return text; }
  }
}
