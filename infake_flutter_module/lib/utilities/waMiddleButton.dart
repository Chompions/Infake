import 'package:infake_flutter_module/utilities/custom_icons.dart';
import 'package:flutter/material.dart';
import 'dart:math' as math;

//* ARROW STACK

class ArrowStack extends StatefulWidget {
  final AnimationController controller;
  final Color middleArrowColor;

  ArrowStack({this.controller, this.middleArrowColor});

  @override
  _ArrowStackState createState() => _ArrowStackState();
}

class _ArrowStackState extends State<ArrowStack> {
  Animation<double> _colorTween;

  @override
  void initState() {
    _colorTween = Tween(
      begin: 3.0,
      end: -3.0,
    ).animate(
      CurvedAnimation(
        parent: widget.controller,
        curve: Interval(0, 0.5),
      ),
    );
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Positioned(
      top: 30,
      child: ShaderMask(
        shaderCallback: (rect) {
          return RadialGradient(
            center: Alignment(0, _colorTween.value),
            radius: 1,
            colors: [
              Colors.white,
              widget.middleArrowColor,
            ],
            // tileMode: TileMode.mirror,
          ).createShader(rect);
        },
        child: Icon(
          MyFlutterApp.swipe,
          size: 80,
          color: Colors.white,
        ),
      ),
    );
  }
}

//* ANIMATED MIDDLE BUTTON

class AnimatedMiddleButton extends StatefulWidget {
  final AnimationController controller;

  AnimatedMiddleButton({this.controller});

  @override
  _AnimatedMiddleButtonState createState() => _AnimatedMiddleButtonState();
}

class _AnimatedMiddleButtonState extends State<AnimatedMiddleButton> {
  @override
  Widget build(BuildContext context) {
    return StaggerAnimation(
      controller: widget.controller.view,
    );
  }
}

//* MIDDLE BUTTON

class MiddleButton extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: Color(0xFF02D65D),
      ),
      constraints: BoxConstraints.tightFor(
        width: 56,
        height: 56,
      ),
      child: Icon(
        Icons.call,
        size: 28,
        color: Colors.white,
      ),
    );
  }
}

//* Shake Curve

class ShakeCurve extends Curve {
  final double _begin;
  final double _end;

  ShakeCurve(this._begin, this._end);

  @override
  double transformInternal(double t) {
    t = ((t - _begin) / (_end - _begin)).clamp(0.0, 1.0) as double;
    var val = (0.1 / 0.8 + t) * math.sin((2 * math.pi * t) / 0.4) + 0.5;
    // var val = math.sin(3 * 2 * math.pi * t) * 0.5 + 0.5;
    return val;
  }
}

//* Stagger Animation

class StaggerAnimation extends StatelessWidget {
  StaggerAnimation({this.controller})
      : _moveUp = Matrix4Tween(
          begin: Matrix4.translationValues(0, 0, 0),
          end: Matrix4.translationValues(0, -30, 0),
        ).animate(
          CurvedAnimation(
            parent: controller,
            curve: Interval(0, 0.25),
          ),
        ),
        _moveDown = Tween<double>(
          begin: 0,
          end: 30,
        ).animate(
          CurvedAnimation(
            parent: controller,
            curve: Interval(0.35, 0.5),
          ),
        ),
        _shake = Tween<double>(
          begin: -2,
          end: 2,
        ).animate(
          CurvedAnimation(
            parent: controller,
            curve: ShakeCurve(0.25, 0.35),
          ),
        );

  Widget _buildAnimation(BuildContext context, Widget child) {
    return Container(
      transform: _moveUp.value,
      child: Container(
        transform: Matrix4.translationValues(
          _shake.value,
          _moveDown.value,
          0,
        ),
        child: MiddleButton(),
      ),
    );
  }

  final Animation controller;
  final Animation _moveUp;
  final Animation _moveDown;
  final Animation _shake;

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      builder: _buildAnimation,
      animation: controller,
    );
  }
}
