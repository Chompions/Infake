import 'dart:convert';

import 'package:infake_flutter_module/main.dart';
import 'package:infake_flutter_module/utilities/waBottomButton.dart';
import 'package:flutter/material.dart';

class FirstWhatsAppIncomingCall extends StatelessWidget {
  final String name;
  final String number;
  final String imageEncoded;

  FirstWhatsAppIncomingCall(this.name, this.number, this.imageEncoded);

  @override
  Widget build(BuildContext context) {
    return Material(
      child: Column(
        children: <Widget>[
          Expanded(
            flex: 4,
            child: Container(
              color: Color(0xFF004B44),
              padding: EdgeInsets.only(top: 5, bottom: 6),
              child: SafeArea(
                child: Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    children: <Widget>[
                      RichText(
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
                      Container(
                        margin: EdgeInsets.all(8.0),
                        decoration: BoxDecoration(
                          color: Colors.white,
                          shape: BoxShape.circle,
                          boxShadow: [
                            BoxShadow(
                              color: Colors.black.withOpacity(.1),
                              spreadRadius: 4,
                              blurRadius: 5,
                            )
                          ],
                        ),
                        child: CircleAvatar(
                          backgroundImage:
                              MemoryImage(base64.decode(imageEncoded)),
                          radius: 47.0,
                        ),
                      ),
                      Container(
                        alignment: Alignment.center,
                        padding: const EdgeInsets.symmetric(horizontal: 8),
                        child: Text(
                          name,
                          overflow: TextOverflow.ellipsis,
                          maxLines: 1,
                          style: TextStyle(
                            fontSize: 30.0,
                            color: Colors.white,
                            fontWeight: FontWeight.w300,
                          ),
                        ),
                      ),
                      Text(
                        "WhatsApp voice call",
                        style: TextStyle(
                          fontSize: 13.0,
                          color: Colors.white,
                          fontWeight: FontWeight.w400,
                        ),
                      )
                    ],
                  ),
                ),
              ),
            ),
          ),
          Expanded(
            flex: 7,
            child: Container(
              width: double.infinity,
              height: double.infinity,
              color: Color(0xFF202930),
              padding: EdgeInsets.only(bottom: 20, left: 30, right: 30),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.end,
                children: <Widget>[
                  BottomButton(
                    nextRoute: FakeCall.FIRST_WHATSAPP_ONGOING_ROUTE,
                    sideButtonColor: Color(0xFF1A2227),
                    middleArrowColor: Color(0xFF1F2831),
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
