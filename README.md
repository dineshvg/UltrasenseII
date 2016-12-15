# UltrasenseII

[Extension on Ultrasense from Jakob Karolus.](https://github.com/jkarolus/UltraSense)

### Features

* UltrasenseII specifically made for collection ground truth data to be used for classification algorithms.
* UltrasenseII controller app made for remotely controlling the UltrasenseII app through bluetooth.

```javascript
1. Doppler shift in the 20Khz signal that is sent from the App is measured. The variations in this signal make the real-time data that will be used to form the ground-truth data.
2. To collect from different people we need to label the data with the start and end time.
3. Since it is audio signals that detect human motion, it woule be better to click the buttons remotely. For devices that do not support OTG, it can be controlled remotely using bluetooth and the Ultrasense controller app.
```

| Android App        | Function           | Reason  |
| ------------- |:-------------:| -----:|
| UltrasenseII      | data collection | In real-world scenario the signals will vary, using this information we can generalize the windows. |
| UltrasenseII controller      | remote control of UltrasenseII      |   To get clean labelled data, and data that will not be corrupted by human movememnt. |


