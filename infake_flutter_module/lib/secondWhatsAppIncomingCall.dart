import 'dart:convert';

import 'package:flutter_statusbar_manager/flutter_statusbar_manager.dart';
import 'package:infake_flutter_module/utilities/waBottomButton.dart';
import 'package:flutter/material.dart';

class SecondWhatsAppIncomingCall extends StatelessWidget {
  final String name;
  final String number;
  final String imageEncoded;

  SecondWhatsAppIncomingCall(this.name, this.number, this.imageEncoded);
  
  Future setUiColor(Color color) async {
    await FlutterStatusbarManager.setColor(color);
    await FlutterStatusbarManager.setNavigationBarColor(color);
  }

  @override
  Widget build(BuildContext context) {
    Color _greenWhatsApp = Color(0xFF054C44);
    setUiColor(_greenWhatsApp);
    
    return Material(
      color: _greenWhatsApp,
      child: Column(
        children: <Widget>[
          Expanded(
            flex: 5,
            child: Container(
              padding: EdgeInsets.only(top: 5, bottom: 6),
              child: SafeArea(
                child: Column(
                  children: <Widget>[
                    /**
                     * Profile Picture
                     */
                    Container(
                      margin: EdgeInsets.only(top: 50),
                      child: CircleAvatar(
                        backgroundImage: MemoryImage(
                            base64.decode(imageEncoded)),
                        radius: 47.0,
                      ),
                    ),
                    /**
                     * Name to display
                     */
                    Container(
                      alignment: Alignment.center,
                      margin: EdgeInsets.only(top: 23, bottom: 15),
                      padding: const EdgeInsets.symmetric(
                          horizontal: 8),
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
                    Text(
                      "WhatsApp voice call",
                      style: TextStyle(
                        fontSize: 16.0,
                        color: Colors.white,
                        fontWeight: FontWeight.w400,
                      ),
                    )
                  ],
                ),
              ),
            ),
          ),
          Expanded(
            flex: 6,
            child: Container(
              padding: EdgeInsets.only(bottom: 15, left: 30, right: 30),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.end,
                children: <Widget>[
                  BottomButton(
                      nextRoute : "/FirstWhatsAppOngoingCall",
                      sideButtonColor: Color(0xFF0A3734),
                      middleArrowColor: Color(0xFF054C44),
                  ),
                  SizedBox(
                    height: 15,
                  ),
                  Text(
                    "Swipe up to accept",
                    style: TextStyle(
                      color: Colors.white.withOpacity(.5),
                      fontSize: 12,
                    ),
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
