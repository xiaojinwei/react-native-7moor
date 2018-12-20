using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace Seven.Moor.RNSevenMoor
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNSevenMoorModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNSevenMoorModule"/>.
        /// </summary>
        internal RNSevenMoorModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNSevenMoor";
            }
        }
    }
}
