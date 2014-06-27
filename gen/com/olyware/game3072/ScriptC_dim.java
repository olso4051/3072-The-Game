/*
 * Copyright (C) 2011-2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This file is auto-generated. DO NOT MODIFY!
 * The source Renderscript file: C:\\Users\\Kyle\\workspace\\3072\\src\\com\\olyware\\game3072\\dim.rs
 */
package com.olyware.game3072;

import android.support.v8.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_dim extends ScriptC {
    private static final String __rs_resource_name = "dim";
    // Constructor
    public  ScriptC_dim(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_dim(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        mExportVar_dimmingValue = 0.40000001f;
        __F32 = Element.F32(rs);
        __U8_4 = Element.U8_4(rs);
    }

    private Element __F32;
    private Element __U8_4;
    private FieldPacker __rs_fp_F32;
    private final static int mExportVarIdx_dimmingValue = 0;
    private float mExportVar_dimmingValue;
    public synchronized void set_dimmingValue(float v) {
        setVar(mExportVarIdx_dimmingValue, v);
        mExportVar_dimmingValue = v;
    }

    public float get_dimmingValue() {
        return mExportVar_dimmingValue;
    }

    public Script.FieldID getFieldID_dimmingValue() {
        return createFieldID(mExportVarIdx_dimmingValue, null);
    }

    //private final static int mExportForEachIdx_root = 0;
    private final static int mExportForEachIdx_dim = 1;
    public Script.KernelID getKernelID_dim() {
        return createKernelID(mExportForEachIdx_dim, 3, null, null);
    }

    public void forEach_dim(Allocation ain, Allocation aout) {
        forEach_dim(ain, aout, null);
    }

    public void forEach_dim(Allocation ain, Allocation aout, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        // check aout
        if (!aout.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        // Verify dimensions
        Type tIn = ain.getType();
        Type tOut = aout.getType();
        if ((tIn.getCount() != tOut.getCount()) ||
            (tIn.getX() != tOut.getX()) ||
            (tIn.getY() != tOut.getY()) ||
            (tIn.getZ() != tOut.getZ()) ||
            (tIn.hasFaces() != tOut.hasFaces()) ||
            (tIn.hasMipmaps() != tOut.hasMipmaps())) {
            throw new RSRuntimeException("Dimension mismatch between input and output parameters!");
        }
        forEach(mExportForEachIdx_dim, ain, aout, null, sc);
    }

}

