import 'dart:convert';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:infake_flutter_module/whatsAppIncomingCall.dart';
import 'package:infake_flutter_module/whatsAppOngoingCall.dart';

void main() => runApp(FakeCall());

class FakeCall extends StatefulWidget {
  static const platform = MethodChannel("method_channel_name");

  @override
  _FakeCallState createState() => _FakeCallState();
}

class _FakeCallState extends State<FakeCall> {
  final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();
  String name = "FlutterDefaultName";
  String number = "FlutterDefaultNumber";
  String route = "/InitialRoute";

  // Get contact data from Android
  Future _getContactDataFromAndroid() async {
    try {
      FakeCall.platform.setMethodCallHandler((call) async {
        if (call.method == "call_method") {
          final jsonResult = await jsonDecode(call.arguments);

          setState(() {
            name = jsonResult['name'] as String;
            number = jsonResult['number'] as String;
            route = jsonResult['route'] as String;
          });

          navigatorKey.currentState.popAndPushNamed(route);
        }
      });
    } on PlatformException catch (e) {
      print(e.message);
    }
  }

  @override
  void initState() {
    _getContactDataFromAndroid();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: "Fake Call",
      navigatorKey: navigatorKey,
      theme: ThemeData(
        fontFamily: 'HelveticNeue',
      ),
      initialRoute: "/InitialRoute",
      routes: {
        "/InitialRoute": (context) => InitialRoute(),
        "/WhatsAppIncomingCall": (context) => WhatsAppIncomingCall(name, number, route),
        "/WhatsAppOngoingCall": (context) => WhatsAppOngoingCall(),
      },
    );
  }
}

class InitialRoute extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Material(
      child: Container(
        color: Colors.black38,
      ),
    );
  }
}
