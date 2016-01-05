# E-Cycler ![img](http://i.imgur.com/fqlA0y5.png?1)
2016 AT&amp;T hackathon 

Our application's goal is to promote recycling in a fun and friendly competition through our social platform.  The app has the user scan the bar code of the item they are going to throw into a recycling bin.  Recycling is then tracked with a"tree" that grows as the user recycles. The user can compare their tree to their friends' and see just who is more eco-friendly, incentivising living greener and recycling more. Per the EULA, the app can collect the information of what each user is purchasing and present targeted advertisements. This advertising program can be opt in, and if the user opts in, they can receive benefits, such as coupons for products they order repeatedly, data incentives, and other in game credits. Public recycle bins equipped with our device can also offer these same benefits, promoting recycling even when the user is away from home (such as being on vacation in Las Vegas).

web app has moved to https://github.com/brianlu365/atthack334r_webapp

-----------------------

This repository contains the source for the android app, ECycler. The E-Cycler app is designed to be as simple as possible, and has an intuitive design:

Initial UI | Barcode scanner
:-----------:|:--------------:
![img](http://i.imgur.com/YcTMxca.png?1) | ![img](http://i.imgur.com/ziW7M0S.png?1)

The app is built using Android Studio and the iD framework, which powers our barcode scanner. When users scan a barcode, the code is sent to our iD entity database where it is queried and matched with the corresponding type of recyclable object. Then, using the M2X API we create a stream and send the matched barcode and user information to the web app. Our web app contains fun statistics and a leaderboard where users can compete to recycle! A public version concept was built and uses this same app, but on an embeded device.

Ecycler was built and completed in the span of 24 hours for the ATT Hackathon

## Team Memebers
* Brian Lu               -> Web App
* Grant Mercer           -> Mobile App
* Jeremy Feliciano       -> Embeded device design and implementation
* Anthony Pallone        -> Graphics artist


