import 'package:flutter/material.dart';

import '../api/api_client.dart';

class AuthScreen extends StatefulWidget {
  final ApiClient apiClient;
  final void Function(AuthResponse auth) onAuthenticated;

  const AuthScreen({
    super.key,
    required this.apiClient,
    required this.onAuthenticated,
  });

  @override
  State<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> {
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _fullNameController = TextEditingController();
  bool _isRegister = false;
  bool _loading = false;

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    _fullNameController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final email = _emailController.text.trim();
    final password = _passwordController.text.trim();
    final fullName = _fullNameController.text.trim();

    if (email.isEmpty || password.isEmpty) {
      _showError('Vui long nhap email va mat khau');
      return;
    }
    if (_isRegister && password.length < 6) {
      _showError('Mat khau toi thieu 6 ky tu');
      return;
    }

    setState(() => _loading = true);
    try {
      final auth = _isRegister
          ? await widget.apiClient.register(
              RegisterRequest(
                email: email,
                password: password,
                fullName: fullName.isEmpty ? null : fullName,
              ),
            )
          : await widget.apiClient.login(
              LoginRequest(
                email: email,
                password: password,
              ),
            );
      widget.onAuthenticated(auth);
    } catch (e) {
      _showError(e.toString());
    } finally {
      if (mounted) {
        setState(() => _loading = false);
      }
    }
  }

  void _showError(String message) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(_isRegister ? 'Dang ky' : 'Dang nhap')),
      body: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 420),
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const Text(
                  'Quan ly can nang & calo',
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.w600),
                ),
                const SizedBox(height: 16),
                TextField(
                  controller: _emailController,
                  keyboardType: TextInputType.emailAddress,
                  decoration: const InputDecoration(labelText: 'Email'),
                ),
                const SizedBox(height: 8),
                TextField(
                  controller: _passwordController,
                  obscureText: true,
                  decoration: const InputDecoration(labelText: 'Mat khau'),
                ),
                if (_isRegister) ...[
                  const SizedBox(height: 8),
                  TextField(
                    controller: _fullNameController,
                    decoration: const InputDecoration(labelText: 'Ho ten (tuy chon)'),
                  ),
                ],
                const SizedBox(height: 16),
                ElevatedButton(
                  onPressed: _loading ? null : _submit,
                  child: _loading
                      ? const SizedBox(
                          height: 18,
                          width: 18,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : Text(_isRegister ? 'Tao tai khoan' : 'Dang nhap'),
                ),
                const SizedBox(height: 8),
                TextButton(
                  onPressed: _loading
                      ? null
                      : () => setState(() => _isRegister = !_isRegister),
                  child: Text(
                    _isRegister
                        ? 'Da co tai khoan? Dang nhap'
                        : 'Chua co tai khoan? Dang ky',
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
