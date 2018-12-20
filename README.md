
# react-native-seven-moor

## Getting started

`$ npm install react-native-seven-moor --save`

### Mostly automatic installation

`$ react-native link react-native-seven-moor`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-seven-moor` and add `RNSevenMoor.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNSevenMoor.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNSevenMoorPackage;` to the imports at the top of the file
  - Add `new RNSevenMoorPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-seven-moor'
  	project(':react-native-seven-moor').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-seven-moor/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-seven-moor')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNSevenMoor.sln` in `node_modules/react-native-seven-moor/windows/RNSevenMoor.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Seven.Moor.RNSevenMoor;` to the usings at the top of the file
  - Add `new RNSevenMoorPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNSevenMoor from 'react-native-seven-moor';

// TODO: What to do with the module?
RNSevenMoor;
```
  