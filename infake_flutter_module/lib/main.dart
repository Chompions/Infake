import 'dart:typed_data';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:infake_flutter_module/whatsAppIncomingCall.dart';
import 'package:infake_flutter_module/whatsAppOngoingCall.dart';

void main() => runApp(FakeCall());

class FakeCall extends StatefulWidget {
  static const PLATFORM_CHANNEL = MethodChannel("platform_channel");

  @override
  _FakeCallState createState() => _FakeCallState();
}

class _FakeCallState extends State<FakeCall> {
  final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();
  String name = "FlutterName";
  String number = "FlutterNumber";
  String route = "/InitialRoute";
  String imageEncoded = "FlutterImage";
  Uint8List bytes;

  // Get contact data from Android
  Future _getContactDataFromAndroid() async {
    try {
      FakeCall.PLATFORM_CHANNEL.setMethodCallHandler((call) async {
        if (call.method == "get_contact") {
          name = call.arguments['name'];
          number = call.arguments['number'];
          route = call.arguments['route'];
          imageEncoded = call.arguments['imageBase64'];

          print(route);
          print(imageEncoded);
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
        "/WhatsAppIncomingCall": (context) => WhatsAppIncomingCall(
            name, number, imageEncoded),
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

// Start cancel method from Android -> for stopping notification
Future<void> startCancelMethodToAndroid() async {
  try {
    FakeCall.PLATFORM_CHANNEL.invokeMethod("start_cancel_method");
  } on PlatformException catch (e) {
    print(e.message);
  }
}