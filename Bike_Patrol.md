## BikePatrol: Project Summary

### 1 Base paper<sup>1</sup> summary

<sub>[1]: Sangkeun Park, Emilia-Stefania Ilincai, Jeungmin Oh, Sujin Kwon, Rabeb Mizouni, and Uichin Lee. 2017. <i>Facilitating Pervasive Community Policing on the Road with Mobile Roadwatch.</i> In Proceedings of the 2017 CHI Conference on Human Factors in Computing Systems (CHI '17). ACM, New York, NY, USA, 3538-3550. DOI: https://doi.org/10.1145/3025453.3025867</sub>

#### 1.1 Overview
In the paper, authors consider community policing (CP) via video footage, captured with smartphones in a dashboard camera fashion.
Although CP has been prevalent in countries like South Korea and India, the reporting process used was tedious.
This resulted in users becoming more passive towards reporting recorded incidents.

The authors have contributed to the CP by developing a mobile application which is user-friendly and maintains user privacy in the same time.

#### 1.2 Conducted study
The study was conducted with 23 participants from a large university (14 members of staff, 9 students).
8 participants (7 males, 1 female) had previous experience of reporting traffic violations.
The study was conducted for 2 weeks and all the participants used cars to commute every day.
The information collected included: the user log data, timestamped history with location data (GPS) and additional contextual data about the incident.
After the field study, a survey was issued to research usability and security (privacy) concerns of the participants.

#### 1.3 Design
To simplify the process of video capture, the authors aimed at incorporating it in a minimalistic touchscreen interaction.
The application improved the reporting process with video footage being captured alongside with location and timestamp information.
This empowered users to seamlessly produce precise details of the incident.
To maintain the privacy, users were given the options to mute and/or edit the length of the video.
The video capture started 15 seconds before user request and lasted 10 seconds, which allowed users to fully capture the situation.
The users were also given the options to choose the video quality between 720 p and 1080 p.

#### 1.4 Evaluation
The participants used the app fairly consistently in the two week field study.
Participants captured 355 events and reported 56 events.
In the capture stage participants captured even trivial events.
However, in the report stage participants tended to report only serious violations, although it did not affect them seriously.
There were other reasons for not reporting captured events which included lack of evidence, low level of seriousness, mistakes and privacy concerns.

#### 1.5 Conclusion
The idea of introducing this application was to promote traffic safety and personal norm of law abidance.
The authors manually sent the reported incidents to the concerned authorities.
The penalties levied for the violators were sent as SMS to the participants.
The reactions of the participants were mostly positive.
They felt good with being a part of this drive.
The participants also felt more responsible while driving since they had the feeling of being watched.
Pervasive mobile recording is a novel medium and has increased in the last few years in countries like India, Korea and USA.

### 2 Bike Patrol
#### 2.1 Constraints
The idea of implementing the concept from the base paper came with a few constraints:

- hardware: usage of automobile was not possible;
- time and manpower: active phase of the project was constrained by the amount of people (only 2) and the time;
- legal issues: the regulations for the video capture in Germany had to be researched.

#### 2.2 Vision
The decision was to implement the idea of CP, while shifting the focus from CP via automobiles to CP by cyclists.
Cycling is a common way to commute for staff members and students at the university.
The implementation could benefit many cyclists, providing previously unavailable safety aspects.
The idea was to build an application that could capture images or videos while cycling.
The smartphone would be attached to the handle bar of the bicycle.
Simple gestures would be used to avoid distractions while cycling.
The media file could be immediately sent to the community page (Twitter).
The timestamp and the GPS location would be attached to this file.
A brief description could also be added.

Eventually, the community could become larger and reward points to reporters, as well as penalty points to the violators - by concerned authorities.
Communities could be localized, working closely with the local law enforcement.

#### 2.3 Challenges
The vision had its internal challenges, e.g.:

- hands-free outlook: was not possible through speech (due to vibro-acoustic noise);
- non-distractive feedback: haptic vibrational feedback through the handle was not feasible (due to vibro-acoustic noise);
- license plate recognition: the idea of using LPR without consent is debatable from the legal standpoint;
- front camera mode: switch to capture scene in the back was not feasible due to being physically blocked by the rider;

#### 2.4 Implementation
##### 2.4.1 Used Libraries
|Library |Version    |  Purpose |
|---|---|---|
|Retrolambda    | 3.6.1 | Java 8 lambda expressions  |
|Realm.io   | 3.4.0 | Local database |
|Fabric Crashlytics   | 2.6.8 | Crash tracking / analytics |
|Fabric Answers   | 1.3.13 | Usage tracking / analytics|
|Twitter 4j    | 4.0.6 + pull requests | Java library for twitter API |
|Sensey  | 1.7.0 | To improve detecting gestures|
|CameraKit       | 0.9.17 | For seamless camera use to record events |
|Android Transcoder       | 0.2.0 | Encoding videos to comply with Twitter requirements |
|Google Play Services | 11.0.2 | Location, Vision|
|Android Maps Utils | 0.5 | Location |
|Iconics + Google Material Typeface  | 2.8.7 | Google Material icon set |


##### 2.4.2 Structure and features
**Home Activity** - This screen opens by default when the application is powered. From here, the use can navigate to the action activity and navigate to the drop down options in the top right of the screen. The options include filed reports, share file and about the application.

**Action activity** - The screen is a live camera feed that can be used to either record or take an image. The swipe up gesture takes a video for 30 seconds and the swipe down gesture takes a photo.

**Report Activity** - From the home activity screen, the user can go to the report activity screen to sync the video/image and add a brief description of the incident.

**Twitter Page** - This is the backend page of the application. Once the media file is synced the users of this community can view it on the Bike Patrol twitter page.

#### 2.5 Conducted study
Google forms were used to collect participants information (6 participants registered for the study, all males, aged between 22 and 30).

The study was conducted in two steps:

1. Evaluation phase. Participants were asked to ride a bike using the application for 30 minutes and try to capture any violation if possible.
2. Feedback phase. The overall experience of using the application and suggestions to improve the quality of the application was checked.

#### 2.6 Evaluation
Participants appreciated functions of application, while they felt more responsible in enforcing safety for cyclists.
The action mode of the application worked as per their expectations.
The most common suggestion was to improve the feedback of the actions through a graphic display.
This application is particularly useful for commuters and less for performance cyclists as the distractions at high speeds are a major concern.

#### 2.7 Conclusion
This application can be especially useful where the number of people commuting by cycles is high.
The task can be made interesting when the local authorities reward points to users for reporting.
This may encourage the community to grow for the notion of crowdsourced safety, vehicular traffic may be more friendly towards cyclists.
The future work may include connecting the application to a genuine local authority web page that can immediately take action against violators.