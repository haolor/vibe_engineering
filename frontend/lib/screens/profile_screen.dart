import 'package:flutter/material.dart';

import '../api/api_client.dart';

class ProfileScreen extends StatefulWidget {
  final ApiClient apiClient;
  final int userId;
  final ProfileResponse? initialProfile;
  final void Function(int profileId) onProfileSaved;

  const ProfileScreen({
    super.key,
    required this.apiClient,
    required this.userId,
    required this.initialProfile,
    required this.onProfileSaved,
  });

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  final _heightCmController = TextEditingController();
  final _heightFtController = TextEditingController();
  final _weightKgController = TextEditingController();
  final _weightLbsController = TextEditingController();
  final _ageController = TextEditingController();
  String _gender = 'Nam';

  bool _loading = false;

  double? _bmi;

  @override
  void initState() {
    super.initState();
    final p = widget.initialProfile;
    if (p != null) {
      _heightCmController.text = p.heightCm.toStringAsFixed(1);
      _weightKgController.text = p.weightKg.toStringAsFixed(1);
      _ageController.text = p.age.toString();
      _gender = p.gender;
      _bmi = p.bmi;
    }
  }

  @override
  void dispose() {
    _heightCmController.dispose();
    _heightFtController.dispose();
    _weightKgController.dispose();
    _weightLbsController.dispose();
    _ageController.dispose();
    super.dispose();
  }

  double? _tryParseDouble(TextEditingController c) {
    final t = c.text.trim();
    if (t.isEmpty) return null;
    return double.tryParse(t);
  }

  int? _tryParseInt(TextEditingController c) {
    final t = c.text.trim();
    if (t.isEmpty) return null;
    return int.tryParse(t);
  }

  Future<void> _submit() async {
    final heightCm = _tryParseDouble(_heightCmController);
    final heightFt = _tryParseDouble(_heightFtController);
    final weightKg = _tryParseDouble(_weightKgController);
    final weightLbs = _tryParseDouble(_weightLbsController);
    final age = _tryParseInt(_ageController);

    if (age == null) {
      _showError('Vui lòng nhập tuổi');
      return;
    }
    if ((heightCm == null && heightFt == null) || (weightKg == null && weightLbs == null)) {
      _showError('Vui lòng nhập chiều cao và cân nặng (1 trong 2 đơn vị mỗi loại)');
      return;
    }

    setState(() => _loading = true);
    try {
      final resp = await widget.apiClient.upsertProfile(
        ProfileRequest(
          userId: widget.userId,
          heightCm: heightCm,
          heightFt: heightFt,
          weightKg: weightKg,
          weightLbs: weightLbs,
          age: age,
          gender: _gender,
        ),
      );

      setState(() => _bmi = resp.bmi);
      widget.onProfileSaved(resp.profileId);
    } catch (e) {
      _showError(e.toString());
    } finally {
      setState(() => _loading = false);
    }
  }

  void _showError(String msg) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(msg)),
    );
  }

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const Text('Nhập BMI / Hồ sơ',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600)),
          const SizedBox(height: 12),

          TextField(
            controller: _heightCmController,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(labelText: 'Chiều cao (cm)'),
          ),
          TextField(
            controller: _heightFtController,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(labelText: 'Chiều cao (ft)'),
          ),
          const SizedBox(height: 8),

          TextField(
            controller: _weightKgController,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(labelText: 'Cân nặng (kg)'),
          ),
          TextField(
            controller: _weightLbsController,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(labelText: 'Cân nặng (pound)'),
          ),
          const SizedBox(height: 8),

          TextField(
            controller: _ageController,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(labelText: 'Tuổi'),
          ),
          const SizedBox(height: 8),

          DropdownButtonFormField<String>(
            value: _gender,
            items: const [
              DropdownMenuItem(value: 'Nam', child: Text('Nam')),
              DropdownMenuItem(value: 'Nữ', child: Text('Nữ')),
            ],
            onChanged: (v) {
              if (v == null) return;
              setState(() => _gender = v);
            },
            decoration: const InputDecoration(labelText: 'Giới tính'),
          ),

          const SizedBox(height: 16),

          ElevatedButton(
            onPressed: _loading ? null : _submit,
            child: _loading
                ? const SizedBox(
                    height: 18,
                    width: 18,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Text('Tính BMI & Lưu'),
          ),

          if (_bmi != null) ...[
            const SizedBox(height: 16),
            Text('BMI hiện tại: ${_bmi!.toStringAsFixed(1)}',
                style: const TextStyle(fontSize: 16)),
          ]
        ],
      ),
    );
  }
}

