<#-- @ftlvariable name="toMany" type="de.greenrobot.daogenerator.ToManyBase" -->
/** Resets a to-many relationship, making the next get call to query for a fresh result. */
@Generated
public synchronized void reset${toMany.name?cap_first}() {
    ${toMany.name} = null;
}