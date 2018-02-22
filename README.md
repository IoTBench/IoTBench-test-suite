

![alt text](http://i68.tinypic.com/25ut821.jpg)

# IoTBench v1.0

**IoTBench** repository contains IoT apps from various IoT platforms. **IoTBench** test-suite is under continual development, always welcoming contributions of the new IoT apps. In particular, we always welcome help towards resolving the issues currently open.

## What is IoTBench?

**IoTBench** is an IoT-specific test corpus, an open repository for evaluating systems designed for IoT app analyses. It is developed under the **SainT** project. You can access the paper on arXiv: [Sensitive Information Tracking in Commodity IoT](http://arxiv.org/).

### What kind of apps does **IoTBench** include?
Currently, **IoTBench** has 236 official SmartThings apps (in smartthings-official folder) and 69 third-party SmartThings apps (in smartThings-third-party folder).
In late 2017, we acquired the official apps from the official [SmartThings Github repo](https://github.com/SmartThingsCommunity/SmartThingsPublic), and the third-party apps are obtained by crawling the [official SmartThings community forum](https://community.smartthings.com/).

**IoTBench** also includes malicious apps. These apps are designed for evaluating the proposed tools for data leaks and for finding the malicious behaviors. We categorize the malicious IoT apps as follows:

- The following folders include malicious apps under *smartThings* folder designed for [**SmartThings**](https://www.smartthings.com/) platform.
    - **smartThings-sensitive-data-leaks**: This folder includes 19 different malicious apps that contain test cases for interesting flow analysis problems as well as for IoT-specific challenges. These apps are developed for the purpose of evaluating the SainT static taint tracking system. These apps can be used to evaluate both static and dynamic taint analysis tools designed for SmartThings apps; It enables assessing a tool's accuracy and effectiveness through the ground truths included in the suite.
        > **List of sensitive data leaks:** You can find a full list data leaks along with their definitions in the Appendix of [SainT paper](http://arxiv.org/).

    - **smartThings-contexIoT:** This folder will include contexIoT SmartThings apps that are used in [ContexIoT: Towards Providing Contextual Integrity to Appified IoT Platforms (NDSS'17)](http://earlence.com/assets/papers/contexiot_ndss17.pdf). These apps include IoT-specific attacks and attacks migrated from mobile phone research. The details of the apps can be found in the paper.

        > List of malicious behaviors: You can find the list of apps in the source code of the app's definition blocks in [contexiot web page](https://sites.google.com/site/iotcontextualintegrity/home).

        > Currently waiting for a response from authors to include the contexIoT apps in **IoTBench**.

- The following folders include apps under *openHAB* folde designed for [**OpenHAB**](https://www.openhab.org/) IoT platform.
     - **openhab-third-party-rules**: This folder includes the apps downloaded by crawling the GitHub openHAB open-source projects. 

### What kind of malicious behavior **IoTBench** apps contain?
The apps include various malicious behaviors. Here, we present the apps in *smartThings/smartThings-sensitive-data-leaks*.
This folder currently includes 19 hand-crafted malicious SmartThings apps that contain data leaks. Sixteen apps have a single data leak, and three have multiple data leaks; a total of 27 data leaks via either Internet and messaging service sinks. We carefully crafted the apps based on official and third-party apps. They include data leaks whose accurate identification through program analysis would require solving problems including *multiple entry points*, *state variables*, *call by reflection*, and *field sensitivity*. Each app comes with ground truth of what data leaks are in the app; this is provided as comment blocks in the app's source code.

### Will you add more IoT apps?
Yes, definitely. We start crawling OpenHAB apps (rules), and the apps will be included very soon. Additionally, you can submit IoT apps whether benign or malicious to **IoTBench**.

### Can I contribute IoT apps to **IoTBench**?
Contributions are welcomed! To contribute additional test cases to **IoTBench**, we ask:
- To fork the project, commit apps along with descriptions (similar to Table 3 in the Appendix of SainT paper) and update this README and then send us a pull request. 
- If you have any questions, please send to [Z. Berkay Celik](https://beerkay.github.io/) 

## Citing this work
``` 
@misc{saint-taint-analysis,
Author = {Z. Berkay Celik, Leonardo Babun and others},
Title = {Sensitive Information Tracking in Commodity IoT},
Year = {2018},
Eprint = {arXiv:2173068},
}
```

## Authors
This repo is managed and maintained by [Z. Berkay Celik](https://beerkay.github.io/) (zbc102@cse.psu.edu).

We would like to thank, among others, the following  authors contributed IoT apps (ordered according to the GitHub contributors page):
- [Z. Berkay Celik](https://beerkay.github.io/) (Penn State University)
- [Leo Babun](http://leobabun.wixsite.com/leo-babun-phd) (Florida International University)
- Amit Kumar Sikder (Florida International University)
- (Your name)

## Copyright
You can find the copyright under each folder of the apps.
