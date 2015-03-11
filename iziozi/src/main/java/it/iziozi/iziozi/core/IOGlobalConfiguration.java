/*
 * Copyright (c) 2014 Martino Lessio -
 * www.martinolessio.com
 * martino [at] iziozi [dot] org
 *
 *
 * This file is part of the IziOzi project.
 *
 * IziOzi is free software:
 * you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * IziOzi is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with IziOzi.
 * If not, see http://www.gnu.org/licenses/.
 */

package it.iziozi.iziozi.core;

import java.util.List;

/**
 * Created by martinolessio on 31/10/14.
 */
public class IOGlobalConfiguration {

    public static Boolean isEditing = false;
    public static Boolean isScanMode = false;
    public static Boolean isInSwapMode = false;
    public static int swapLevelIndex = -1;

    public static List<IOSpeakableImageButton> firstList = null;
    public static int firstIndex = -1;
    public static IOSpeakableImageButton swapStorage = null;

    public static final String IO_LAST_BOARD_USED = "last_board_used";


}
