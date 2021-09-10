import 'dart:convert';

import 'package:flutter_statusbar_manager/flutter_statusbar_manager.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

import 'main.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:async';

class SecondWhatsAppOngoingCall extends StatelessWidget {
  final String name;
  final String number;
  final String imageEncoded;

  SecondWhatsAppOngoingCall(this.name, this.number, this.imageEncoded);

  Future setUiColor(Color color) async {
    await FlutterStatusbarManager.setColor(color);
    await FlutterStatusbarManager.setNavigationBarColor(color);
  }

  @override
  Widget build(BuildContext context) {
    Color _greenWhatsApp = Color(0xFF1B5C54);

    startCancelMethodToAndroid();
    setUiColor(_greenWhatsApp);

    return Material(
      textStyle: TextStyle(
        fontFamily: 'HelveticNeue',
      ),
      child: Column(
        children: <Widget>[
          Expanded(
            flex: 10,
            child: Container(
              width: double.infinity,
              padding: EdgeInsets.only(top: 4, bottom: 17, left: 5, right: 10),
              color: _greenWhatsApp,
              child: SafeArea(
                child: Column(
                  children: <Widget>[
                    Stack(
                      alignment: Alignment.center,
                      children: <Widget>[
                        Align(
                          alignment: Alignment.centerLeft,
                          child: Icon(
                            Icons.expand_more,
                            color: Colors.white,
                            size: 38,
                          ),
                        ),
                        Align(
                          alignment: Alignment.center,
                          child: RichText(
                            text: TextSpan(children: [
                              WidgetSpan(
                                alignment: PlaceholderAlignment.middle,
                                child: Icon(
                                  Icons.lock,
                                  color: Colors.white.withOpacity(.6),
                                  size: 14.0,
                                ),
                              ),
                              TextSpan(
                                text: "  End-to-end encrypted",
                                style: TextStyle(
                                  fontSize: 14.0,
                                  color: Colors.white.withOpacity(.6),
                                ),
                              ),
                            ]),
                          ),
                        ),
                        Align(
                          alignment: Alignment.centerRight,
                          child: Icon(
                            Icons.person_add,
                            color: Colors.white,
                            size: 25,
                          ),
                        )
                      ],
                    ),
                    Container(
                      alignment: Alignment.center,
                      margin: EdgeInsets.only(top: 25, bottom: 17),
                      padding: const EdgeInsets.symmetric(horizontal: 8),
                      child: Text(
                        name,
                        overflow: TextOverflow.ellipsis,
                        maxLines: 1,
                        style: TextStyle(
                          fontSize: 26.0,
                          color: Colors.white,
                          fontWeight: FontWeight.w400,
                        ),
                      ),
                    ),
                    TickingTimer(),
                  ],
                ),
              ),
            ),
          ),
          Expanded(
            flex: 25,
            child: Stack(
              alignment: Alignment.bottomCenter,
              children: <Widget>[
                Container(
                  decoration: BoxDecoration(
                    color: Colors.white,
                    image: DecorationImage(
                      image: MemoryImage(base64.decode(imageEncoded)),
                      fit: BoxFit.fitHeight,
                    ),
                  ),
                ),
              ],
            ),
          ),
          Expanded(
            flex: 6,
            child: Container(
              decoration: BoxDecoration(
                borderRadius: BorderRadius.only(
                  topLeft: Radius.circular(14),
                  topRight: Radius.circular(14),
                ),
                color: _greenWhatsApp,
              ),
              child: Column(
                children: [
                  Transform(
                    transform: Matrix4.diagonal3Values(1.5, 1, 1),
                    alignment: Alignment.center,
                    child: FaIcon(
                      FontAwesomeIcons.chevronUp,
                      color: Color(0xFF2AA799),
                      size: 27,
                    ),
                  ),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: <Widget>[
                      Icon(
                        Icons.volume_up,
                        color: Colors.white,
                        size: 30,
                      ),
                      FaIcon(
                        FontAwesomeIcons.video,
                        color: Colors.white,
                        size: 22,
                      ),
                      SizedBox(
                        width: 12,
                        child: Icon(
                          Icons.mic_off,
                          color: Colors.white,
                          size: 30,
                        ),
                      ),
                      RawMaterialButton(
                        onPressed: () {
                          SystemNavigator.pop();
                        },
                        elevation: 2,
                        fillColor: Color(0xFFE91C43),
                        child: Icon(
                          Icons.call_end,
                          size: 29,
                          color: Colors.white,
                        ),
                        constraints: BoxConstraints.tightFor(
                          width: 50,
                          height: 50,
                        ),
                        shape: CircleBorder(),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class TickingTimer extends StatefulWidget {
  @override
  _TickingTimerState createState() => _TickingTimerState();
}

class _TickingTimerState extends State<TickingTimer> {
  int _intMin = 0;
  int _intSec = 00;
  String _seconds = "00";
  String _minutes = "0";
  Timer _callTimer;

  void _startTimer() {
    _callTimer ??= Timer.periodic(Duration(seconds: 1), (timer) {
      if (_intSec < 9) {
        setState(() {
          _intSec++;
          _seconds = "0$_intSec";
        });
      } else if (_intSec == 59) {
        setState(() {
          _intSec = 0;
          _intMin++;
          _seconds = "0$_intSec";
          _minutes = "$_intMin";
        });
      } else {
        setState(() {
          _intSec++;
          _seconds = "$_intSec";
        });
      }
    });
  }

  @override
  void initState() {
    _startTimer();
    super.initState();
  }

  @override
  void dispose() {
    _callTimer.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Text(
      "$_minutes:$_seconds",
      style: TextStyle(
        fontSize: 17.0,
        color: Colors.white,
        fontWeight: FontWeight.w400,
      ),
    );
  }
}
