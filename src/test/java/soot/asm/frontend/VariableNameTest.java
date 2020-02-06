package soot.asm.frontend;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2018 Raja Vallée-Rai and others
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

import org.junit.BeforeClass;
import org.junit.Test;
import soot.*;
import soot.util.Chain;

import static org.junit.Assert.*;


/**
 * Abstract base class for tests for the ASM frontend
 *
 * @author Alexander Schwarz
 */
public class VariableNameTest {

  private static final String CLASS_NAME = "soot.asm.frontend.VariableNames";

  @BeforeClass
  public static void setup() {
    TestHelper.loadClass(CLASS_NAME);
  }

  @Test
  public void differentVarNamesForSameIndex() {
    SootClass cl = Scene.v().getSootClass(CLASS_NAME);
    SootMethod method = cl.getMethodByName("differentVarsSameIndex");
    Chain<Local> locals = method.getActiveBody().getLocals();
    assertLocalExists("a", locals);
    assertLocalExists("b", locals);
    assertLocalExists("c", locals);
    assertLocalExists("d", locals);
    assertLocalExists("result", locals);
    assertLocalExists("this", locals);
    assertEquals(6, locals.size());
  }

  private void assertLocalExists(String name, Iterable<Local> locals) {
    for (Local local : locals) {
      if (local.getName().equals(name))
        return;
    }
    fail("Local variable '" +  name + "' is missing.");
  }

}
