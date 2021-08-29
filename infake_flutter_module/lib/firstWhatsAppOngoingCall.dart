import 'dart:convert';

import 'main.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:async';

class FirstWhatsAppOngoingCall extends StatelessWidget {
  final String name;
  final String number;
  final String imageEncoded;

  FirstWhatsAppOngoingCall(this.name, this.number, this.imageEncoded);

  @override
  Widget build(BuildContext context) {
    startCancelMethodToAndroid();

    return Material(
      textStyle: TextStyle(
        fontFamily: 'HelveticNeue',
      ),
      child: Column(
        children: <Widget>[
          Expanded(
            flex: 8,
            child: Container(
              width: double.infinity,
              padding: EdgeInsets.only(top: 4, bottom: 17, left: 5, right: 10),
              color: Color(0xFF004B44),
              child: SafeArea(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
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
                      padding: const EdgeInsets.symmetric(
                          horizontal: 8),
                      child: Text(
                        name,
                        overflow: TextOverflow.ellipsis,
                        maxLines: 1,
                        style: TextStyle(
                          fontSize: 28.0,
                          color: Colors.white,
                          fontWeight: FontWeight.w300,
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
                Positioned(
                  bottom: 40,
                  child: RawMaterialButton(
                    onPressed: () {
                      SystemNavigator.pop();
                    },
                    elevation: 2,
                    fillColor: Color(0xFFE91C43),
                    child: Icon(
                      Icons.call_end,
                      size: 32,
                      color: Colors.white,
                    ),
                    constraints: BoxConstraints.tightFor(
                      width: 60,
                      height: 60,
                    ),
                    shape: CircleBorder(),
                  ),
                ),
              ],
            ),
          ),
          Expanded(
            flex: 4,
            child: Container(
              color: Color(0xFF004B44),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: <Widget>[
                  Icon(
                    Icons.volume_up,
                    color: Color(0xFFB4CAC7),
                    size: 28,
                  ),
                  Icon(
                    Icons.videocam,
                    color: Color(0xFFB4CAC7),
                    size: 31,
                  ),
                  Icon(
                    Icons.mic_off,
                    color: Color(0xFFB4CAC7),
                    size: 28,
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
        fontSize: 15.0,
        color: Colors.white,
      ),
    );
  }
}
