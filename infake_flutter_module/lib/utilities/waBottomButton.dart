import 'package:infake_flutter_module/utilities/custom_icons.dart';
import 'package:flutter/material.dart';
import 'package:infake_flutter_module/whatsAppOngoingCall.dart';
import 'dart:async';
import 'waMiddleButton.dart';
import 'dart:io';

class BottomButton extends StatefulWidget {
  final String name;
  // final File image;

  // BottomButton(this.name, this.image);
  BottomButton(this.name);

  @override
  _BottomButtonState createState() => _BottomButtonState();
}

class _BottomButtonState extends State<BottomButton> with TickerProviderStateMixin {
  Timer _incomingCallTimer;
  int _incomingCallDuration = 20;
  AnimationController _controller;
  bool _visibleAnimation = true;
  double _buttonPosition = 0.0;

  void startTimer() {
    _incomingCallTimer = Timer.periodic(Duration(seconds: 1), (timer) {
      setState(() {
        if (_incomingCallDuration < 1) {
          _incomingCallTimer.cancel();
          Navigator.pop(context);
        } else {
          _incomingCallDuration = _incomingCallDuration - 1;
          // print(incomingCallDuration);
        }
      });
    });
  }

  @override
  void initState() {
    startTimer();
    _controller = AnimationController(
      duration: const Duration(seconds: 2),
      vsync: this,
    )..repeat();
    super.initState();
  }

  @override
  void dispose() {
    _incomingCallTimer.cancel();
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      crossAxisAlignment: CrossAxisAlignment.end,
      children: <Widget>[
        RawMaterialButton(
          onPressed: () {
            Navigator.pop(context);
          },
          elevation: 0,
          fillColor: Color(0xFF1A2227),
          child: Icon(
            Icons.call_end,
            size: 27,
            color: Colors.red,
          ),
          constraints: BoxConstraints.tightFor(
            width: 55,
            height: 55,
          ),
          shape: CircleBorder(),
        ),
        Container(
          alignment: Alignment.bottomCenter,
          child: Stack(
            alignment: Alignment.bottomCenter,
            children: [
              Container(
                height: 200,
                width: 100,
              ),
              ArrowStack(
                controller: _controller,
              ),
              GestureDetector(
                onPanStart: (details) {
                  setState(() {
                    _visibleAnimation = false;
                  });
                },
                onPanUpdate: (details) {
                  setState(() {
                    _buttonPosition = details.localPosition.dy.clamp(-200.0, 0.0);
                  });
                  // print(buttonPosition);
                },
                onPanEnd: (details) {
                  setState(() {
                    if (_buttonPosition == -200.0) {
                      Navigator.popAndPushNamed(context, '/WhatsAppOngoingCall');
                    } else {
                      _buttonPosition = 0.0;
                      _visibleAnimation = true;
                    }
                  });
                },
                child: Transform.translate(
                  offset: Offset(0.0, _buttonPosition),
                  child: Container(
                    child: Stack(
                      children: <Widget>[
                        Visibility(
                          visible: _visibleAnimation == false,
                          child: MiddleButton(),
                        ),
                        Visibility(
                          visible: _visibleAnimation == true,
                          child: AnimatedMiddleButton(
                            controller: _controller,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
        RawMaterialButton(
          onPressed: () {
            Navigator.pop(context);
          },
          elevation: 0,
          fillColor: Color(0xFF1A2227),
          child: Icon(
            MyFlutterApp.message_reply,
            size: 20,
            color: Colors.white,
          ),
          constraints: BoxConstraints.tightFor(
            width: 55,
            height: 55,
          ),
          shape: CircleBorder(),
        ),
      ],
    );
  }
}
