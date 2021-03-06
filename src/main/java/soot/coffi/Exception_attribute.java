package soot.coffi;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 Clark Verbrugge
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

/**
 * There should be exactly one Exceptions attribute in every method, indicating the types of exceptions the method might
 * throw.
 *
 * @see attribute_info
 * @see method_info#attributes
 * @author Clark Verbrugge
 */
public class Exception_attribute extends attribute_info {
  /** Length of exception table array. */
  public int number_of_exceptions;
  /**
   * Constant pool indices of CONSTANT_Class types representing exceptions the associated method might throw.
   *
   * @see CONSTANT_Class_info
   */
  public int exception_index_table[];
}
