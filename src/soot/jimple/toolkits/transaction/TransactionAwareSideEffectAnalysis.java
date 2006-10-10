package soot.jimple.toolkits.transaction;
/* Soot - a J*va Optimization Framework
 * Copyright (C) 2003 Ondrej Lhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.*;
import soot.jimple.toolkits.pointer.MethodRWSet;
import soot.jimple.toolkits.pointer.RWSet;
import soot.jimple.toolkits.pointer.SiteRWSet;
import soot.jimple.toolkits.pointer.StmtRWSet;

import java.util.*;
import soot.util.*;

/** Generates side-effect information from a PointsToAnalysis. */
public class TransactionAwareSideEffectAnalysis {
	PointsToAnalysis pa;
	CallGraph cg;
	Map methodToNTReadSet = new HashMap();
	Map methodToNTWriteSet = new HashMap();
	int rwsetcount = 0;
	TransitiveTargets tt;
	Collection transactions;
	
	public void findNTRWSets( SootMethod method ) {
		if( methodToNTReadSet.containsKey( method )
				&& methodToNTWriteSet.containsKey( method ) ) return;
		
		MethodRWSet read = null;
		MethodRWSet write = null;
		for( Iterator sIt = method.retrieveActiveBody().getUnits().iterator(); sIt.hasNext(); ) {
			final Stmt s = (Stmt) sIt.next();
			
			boolean ignore = false;
			if(transactions != null)
			{
				Iterator tnIt = transactions.iterator();
				while(tnIt.hasNext())
				{
					Transaction tn = (Transaction) tnIt.next();
					if(tn.units.contains(s))
					{
						ignore = true;
						break;
					}
				}
			}
			
			if(!ignore)
			{
				RWSet ntr = ntReadSet( method, s );
				if( ntr != null ) {
					if( read == null ) read = new MethodRWSet();
					read.union( ntr );
				}
				RWSet ntw = ntWriteSet( method, s );
				if( ntw != null ) {
					if( write == null ) write = new MethodRWSet();
					write.union( ntw );
				}
			}
		}
		methodToNTReadSet.put( method, read );
		methodToNTWriteSet.put( method, write );
		SootClass c = method.getDeclaringClass();
	}
	
	public RWSet nonTransitiveReadSet( SootMethod method ) {
		findNTRWSets( method );
		return (RWSet) methodToNTReadSet.get( method );
	}
	
	public RWSet nonTransitiveWriteSet( SootMethod method ) {
		findNTRWSets( method );
		return (RWSet) methodToNTWriteSet.get( method );
	}
	
	public TransactionAwareSideEffectAnalysis( PointsToAnalysis pa, CallGraph cg, Collection transactions ) {
		this.pa = pa;
		this.cg = cg;
		this.tt = new TransitiveTargets( cg );
		this.transactions = transactions;
	}
	
	private RWSet ntReadSet( SootMethod method, Stmt stmt )
	{
		if( stmt instanceof AssignStmt ) {
			AssignStmt a = (AssignStmt) stmt;
			Value r = a.getRightOp();
			return addValue( r, method, stmt );
		}
		return null;
	}
	
	public RWSet approximatedReadSet( SootMethod method, Stmt stmt, Value v)
	{// used for stmts with method calls where the effect of the method call should be approximated by 0 or 1 reads (plus reads of all args)
		RWSet ret = new SiteRWSet();
		if(v != null)
		{
			if( v instanceof Local )
			{
				Local vLocal = (Local) v;
				PointsToSet base = pa.reachingObjects( vLocal );
				StmtRWSet sSet = new StmtRWSet();
				sSet.addFieldRef( base, stmt );
				ret.union(sSet);
			}
			else if( v instanceof FieldRef)
			{
				ret.union(addValue(v, method, stmt));
			}
		}
		if(stmt.containsInvokeExpr())
		{
			for(int i = 0; i < stmt.getInvokeExpr().getArgCount(); i++)
				ret.union(addValue( stmt.getInvokeExpr().getArg(i), method, stmt ));
		}
		if( stmt instanceof AssignStmt ) {
			AssignStmt a = (AssignStmt) stmt;
			Value r = a.getRightOp();
			ret.union(addValue( r, method, stmt ));
		}
		return ret;
	}
	
	public RWSet readSet( SootMethod method, Stmt stmt ) {
		RWSet ret = null;
		Iterator targets = tt.iterator( stmt );
		while( targets.hasNext() ) {
			SootMethod target = (SootMethod) targets.next();
			if( target.isNative() ) {
				if( ret == null ) ret = new SiteRWSet();
				ret.setCallsNative();
			} 
			else if( target.isConcrete() ) 
			{
				RWSet ntr = nonTransitiveReadSet(target);
				if( ntr != null ) {
					if( ret == null ) ret = new SiteRWSet();
					ret.union( ntr );
				}
			}
		}
		if( ret == null ) return ntReadSet( method, stmt );
		ret.union( ntReadSet( method, stmt ) );
		return ret;
	}
	
	private RWSet ntWriteSet( SootMethod method, Stmt stmt ) {
		if( stmt instanceof AssignStmt ) {
			AssignStmt a = (AssignStmt) stmt;
			Value l = a.getLeftOp();
			return addValue( l, method, stmt );
		}
		return null;
	}
	
	public RWSet approximatedWriteSet( SootMethod method, Stmt stmt, Value v )
	{// used for stmts with method calls where the effect of the method call should be approximated by 0 or 1 writes
		RWSet ret = new SiteRWSet();
		if(v != null)
		{
			if( v instanceof Local )
			{
				Local vLocal = (Local) v;
				PointsToSet base = pa.reachingObjects( vLocal );
				StmtRWSet sSet = new StmtRWSet();
				sSet.addFieldRef( base, stmt );
				ret.union(sSet);
			}
			else if( v instanceof FieldRef)
			{
				ret.union(addValue(v, method, stmt));
			}
		}
		if( stmt instanceof AssignStmt ) {
			AssignStmt a = (AssignStmt) stmt;
			Value l = a.getLeftOp();
			ret.union(addValue( l, method, stmt ));
		}
		return ret;
	}
	
	public RWSet writeSet( SootMethod method, Stmt stmt ) {
		RWSet ret = null;
		Iterator targets = tt.iterator( stmt );
		while( targets.hasNext() ) {
			SootMethod target = (SootMethod) targets.next();
			if( target.isNative() ) {
				if( ret == null ) ret = new SiteRWSet();
				ret.setCallsNative();
			} else if( target.isConcrete() ) {
				RWSet ntw = nonTransitiveWriteSet(target);
				if( ntw != null ) {
					if( ret == null ) ret = new SiteRWSet();
					ret.union( ntw );
				}
			}
		}
		if( ret == null ) return ntWriteSet( method, stmt );
		ret.union( ntWriteSet( method, stmt ) );
		return ret;
	}
	
	protected RWSet addValue( Value v, SootMethod m, Stmt s ) {
		RWSet ret = null;
		if( v instanceof InstanceFieldRef ) {
			InstanceFieldRef ifr = (InstanceFieldRef) v;
			PointsToSet base = pa.reachingObjects( (Local) ifr.getBase() );
			ret = new StmtRWSet();
			ret.addFieldRef( base, ifr.getField() );
		} else if( v instanceof StaticFieldRef ) {
			StaticFieldRef sfr = (StaticFieldRef) v;
			ret = new StmtRWSet();
			ret.addGlobal( sfr.getField() );
		} else if( v instanceof ArrayRef ) {
			ArrayRef ar = (ArrayRef) v;
			PointsToSet base = pa.reachingObjects( (Local) ar.getBase() );
			ret = new StmtRWSet();
			ret.addFieldRef( base, PointsToAnalysis.ARRAY_ELEMENTS_NODE );
		}
		return ret;
	}
	
	public String toString() {
		return "TransactionAwareSideEffectAnalysis: PA="+pa+" CG="+cg;
	}
}

